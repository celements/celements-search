package com.celements.search.lucene.index.queue;

import java.util.concurrent.PriorityBlockingQueue;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.search.lucene.index.IndexData;

/**
 * like {@link LuceneIndexingPriorityQueue} but behaves as blocking priority queue instead
 */
@Singleton
@ThreadSafe
@Component(LuceneIndexingBlockingPriorityQueue.NAME)
public class LuceneIndexingBlockingPriorityQueue extends LuceneIndexingPriorityQueue {

  public static final String NAME = "priority-blocking";

  private final PriorityBlockingQueue<IndexQueueElement> queue = new PriorityBlockingQueue<>();

  @Override
  protected PriorityBlockingQueue<IndexQueueElement> getQueue() {
    return queue;
  }

  // override to remove synchronization of super class
  @Override
  public int getSize() {
    return getQueue().size();
  }

  // override to remove synchronization of super class
  @Override
  public boolean isEmpty() {
    return getQueue().isEmpty();
  }

  @Override
  public synchronized IndexData take() throws InterruptedException {
    return getMap().remove(getQueue().take().id);
  }

}
