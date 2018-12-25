package com.celements.search.lucene.index.queue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Condition;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

import com.celements.logging.LogLevel;
import com.celements.logging.LogUtils;
import com.celements.search.lucene.index.IndexData;
import com.celements.search.lucene.index.LuceneDocId;
import com.celements.search.lucene.index.queue.CloseableReentrantLock.CloseableLock;
import com.google.common.collect.FluentIterable;

/**
 * This class represents a queue for lucene data to be indexed. It's elements are ordered by their
 * respective {@link IndexQueuePriority}. For equal priority the queue behaves as FIFO.
 */
@Singleton
@ThreadSafe
@Component(LuceneIndexingPriorityQueue.NAME)
public class LuceneIndexingPriorityQueue implements LuceneIndexingQueue {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneIndexingPriorityQueue.class);

  public static final String NAME = "priority";

  /**
   * underlying priority queue, see {@link IndexQueueElement} for behaviour
   */
  private final Queue<IndexQueueElement> queue = new PriorityQueue<>();

  /**
   * map containing actual index data and providing constant lookup
   */
  private final Map<LuceneDocId, IndexData> map = new HashMap<>();

  /**
   * lock providing thread safety on methods accessing {@link #queue} and {@link #map} and manages
   * blocking behaviour, see {@link #notEmpty} and {@link #notFull}
   */
  private final CloseableReentrantLock lock = new CloseableReentrantLock();

  /**
   * condition object signaling queue not empty. manages blocking behaviour of {@link #take()}
   */
  private final Condition notEmpty = lock.newCondition();

  /**
   * condition objects signaling queue not full. object per priority in order to signal threads
   * with higher priority first. manages blocking behaviour of {@link #put(IndexData)}
   */
  private final Map<IndexQueuePriority, CountingCondition> notFull = FluentIterable.from(
      IndexQueuePriority.values()).toMap(new CountingCondition.CreateFunction<>(lock,
          getAwaitSeconds()));

  @Override
  public int getSize() {
    try (CloseableLock closeableLock = lock.open()) {
      return queue.size();
    }
  }

  @Override
  public boolean isEmpty() {
    try (CloseableLock closeableLock = lock.open()) {
      return queue.isEmpty();
    }
  }

  /**
   * {@inheritDoc}
   * Time complexity O(1)
   */
  @Override
  public boolean contains(LuceneDocId id) {
    try (CloseableLock closeableLock = lock.open()) {
      return map.containsKey(id);
    }
  }

  @Override
  public void add(IndexData data) {
    try {
      putInternal(data, false);
    } catch (InterruptedException exc) {
      throw new RuntimeException("should not happen", exc);
    }
  }

  @Override
  public void put(IndexData data) throws InterruptedException {
    putInternal(data, true);
  }

  /**
   * Time complexity O(log(n))
   */
  private void putInternal(IndexData data, boolean blocking) throws InterruptedException {
    log(LogLevel.INFO, "put [{}] priority [{}]", data.getId(), data.getPriority());
    try (CloseableLock closeableLock = lock.open()) {
      if (map.put(data.getId(), data) == null) {
        IndexQueueElement element = new IndexQueueElement(data.getId(), data.getPriority());
        while (blocking && (getSize() >= getMaxQueueSize())) {
          log(LogLevel.DEBUG, "waiting put");
          notFull.get(element.getPriority()).await();
          log(LogLevel.DEBUG, "resume put");
        }
        queue.add(element);
      } else {
        log(LogLevel.DEBUG, "already in queue [{}]", data.getId());
      }
      notEmpty.signal();
      log(LogLevel.DEBUG, "signaled notEmpty");
    }
  }

  /**
   * {@inheritDoc} Head is the oldest element in the queue with the highest priority.
   */
  @Override
  public IndexData remove() throws NoSuchElementException {
    try {
      return takeInternal(false);
    } catch (InterruptedException exc) {
      throw new RuntimeException("should not happen", exc);
    }
  }

  @Override
  public IndexData take() throws InterruptedException {
    return takeInternal(true);
  }

  /**
   * Time complexity O(log(n))
   */
  private IndexData takeInternal(boolean blocking) throws InterruptedException {
    IndexData data;
    try (CloseableLock closeableLock = lock.open()) {
      while (blocking && (getSize() == 0)) {
        log(LogLevel.DEBUG, "waiting take");
        notEmpty.await();
        log(LogLevel.DEBUG, "resume take");
      }
      data = map.remove(queue.remove().getId());
      signalNextPriorityWaiter();
    }
    log(LogLevel.INFO, "took [{}] priority [{}]", data.getId(), data.getPriority());
    return data;
  }

  private void signalNextPriorityWaiter() {
    Iterator<IndexQueuePriority> iter = IndexQueuePriority.list().reverse().iterator();
    boolean signaled = false;
    IndexQueuePriority priority;
    CountingCondition condition;
    do {
      priority = iter.next();
      condition = notFull.get(priority);
      signaled |= condition.signalWaiterIfAny();
    } while (!signaled && iter.hasNext());
    if (signaled) {
      log(LogLevel.DEBUG, "signaled notFull [{}]", priority);
    } else {
      log(LogLevel.DEBUG, "nothing to signal", priority);
    }
  }

  private static int getMaxQueueSize() {
    return 1000; // TODO from cfg
  }

  private static int getAwaitSeconds() {
    return 10; // TODO from cfg
  }

  private void log(LogLevel level, String msg, Object... args) {
    LogUtils.log(LOGGER, level, "[{}] [{}] " + msg, Thread.currentThread().getName(), getSize(),
        msg, args);
  }

}
