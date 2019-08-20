package com.celements.search.lucene.index.queue;

import static com.google.common.base.Preconditions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
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
   * blocking behaviour, see @ {@link #empty}, {link #notEmpty} and {@link #notFull}
   */
  private final CloseableReentrantLock lock = new CloseableReentrantLock();

  /**
   * condition object signaling queue empty. manages blocking behaviour of {@link #awaitEmpty()}
   */
  private final Condition empty = lock.newCondition();

  /**
   * condition object signaling queue not empty. manages blocking behaviour of {@link #take()}
   */
  private final Condition notEmpty = lock.newCondition();

  /**
   * condition objects signaling queue not full. object per priority in order to signal threads
   * with higher priority first. manages blocking behaviour of {@link #put(IndexData)}
   */
  private final PriorityCondition notFull = new PriorityCondition(() -> lock.newCondition());

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

  public boolean isMaxedOut() {
    return getSize() >= getMaxQueueSize();
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

  private IndexQueuePriority getCurrentPriority() {
    return Optional.ofNullable(queue.peek())
        .map(IndexQueueElement::getPriority)
        .orElse(IndexQueuePriority.LOWEST);
  }

  @Override
  public void add(IndexData data) {
    try {
      add(data, () -> {});
    } catch (InterruptedException exc) {
      throw new RuntimeException("should not happen", exc);
    }
  }

  @Override
  public void put(IndexData data) throws InterruptedException {
    add(data, getNotFullWaitable(data.getPriority()));
  }

  private Waitable getNotFullWaitable(IndexQueuePriority priority) {
    return () -> {
      while (isMaxedOut() && (priority.compareTo(getCurrentPriority()) <= 0)) {
        log(LogLevel.DEBUG, "waiting on not full");
        notFull.await(priority);
      }
      log(LogLevel.DEBUG, "resume after not full");
    };
  }

  /**
   * Time complexity O(log(n))
   */
  private void add(IndexData data, Waitable waitable) throws InterruptedException {
    log(LogLevel.INFO, "put [{}] priority [{}]", data.getId(), data.getPriority());
    try (CloseableLock closeableLock = lock.open()) {
      if (map.put(data.getId(), data) == null) {
        waitable.await();
        queue.add(new IndexQueueElement(data.getId(), data.getPriority()));
      } else {
        log(LogLevel.DEBUG, "already in queue [{}]", data.getId());
      }
      notEmpty.signalAll();
      log(LogLevel.DEBUG, "signaled notEmpty");
    }
  }

  /**
   * {@inheritDoc} Head is the oldest element in the queue with the highest priority.
   */
  @Override
  public IndexData remove() throws NoSuchElementException {
    try {
      return remove(() -> {});
    } catch (InterruptedException exc) {
      throw new RuntimeException("should not happen", exc);
    }
  }

  @Override
  public IndexData take() throws InterruptedException {
    return remove(getNotEmptyWaitable());
  }

  private Waitable getNotEmptyWaitable() {
    return () -> {
      while (isEmpty()) {
        log(LogLevel.DEBUG, "waiting on not empty");
        notEmpty.await();
      }
      log(LogLevel.DEBUG, "resume after not empty");
    };
  }

  /**
   * Time complexity O(log(n))
   */
  private IndexData remove(Waitable waitable) throws InterruptedException {
    IndexData data;
    try (CloseableLock closeableLock = lock.open()) {
      waitable.await();
      data = map.remove(queue.remove().getId());
      if (isEmpty()) {
        empty.signalAll();
      }
      if (!isMaxedOut() || !data.getPriority().equals(getCurrentPriority())) {
        notFull.signalAllOfNextPriority();
        log(LogLevel.DEBUG, "signaled notFull");
      }
    }
    log(LogLevel.INFO, "took [{}] priority [{}]", data.getId(), data.getPriority());
    return data;
  }

  // TODO move to interface
  public void awaitEmpty() throws InterruptedException {
    try (CloseableLock closeableLock = lock.open()) {
      while (!isEmpty()) {
        empty.await();
      }
    }
  }

  private static int getMaxQueueSize() {
    int maxQueueSize = 1000;
    checkArgument(maxQueueSize > 0);
    return maxQueueSize; // TODO from cfg
  }

  private void log(LogLevel level, String msg, Object... args) {
    LogUtils.log(LOGGER, level, "[{}] [{}] " + msg, Thread.currentThread().getName(), getSize(),
        msg, args);
  }

  @FunctionalInterface
  private interface Waitable {

    void await() throws InterruptedException;

  }

}
