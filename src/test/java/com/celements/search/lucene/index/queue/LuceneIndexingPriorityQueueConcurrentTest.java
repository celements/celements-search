package com.celements.search.lucene.index.queue;

import static com.celements.search.lucene.index.queue.IndexQueuePriority.*;
import static org.junit.Assert.*;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.search.lucene.index.IndexData;
import com.celements.search.lucene.index.WikiData;
import com.xpn.xwiki.web.Utils;

public class LuceneIndexingPriorityQueueConcurrentTest {

  private LuceneIndexingPriorityQueue queue;
  private List<Thread> threads = new ArrayList<>();
  private AtomicBoolean running = new AtomicBoolean();

  @Before
  public void prepare() {
    queue = (LuceneIndexingPriorityQueue) Utils.getComponent(LuceneIndexingQueue.class,
        LuceneIndexingPriorityQueue.NAME);
    running.set(true);
  }

  @After
  public void cleanup() {
    running.set(false);
    threads.stream().forEach(Thread::interrupt);
  }

  @Test
  public void test_manual() throws Exception {
    queue.add(newData(6).setPriority(LOW));
    queue.add(newData(0).setPriority(HIGHEST));
    queue.add(newData(2).setPriority(HIGH));
    queue.add(newData(8).setPriority(LOWEST));
    queue.add(newData(9).setPriority(LOWEST));
    queue.add(newData(4));
    queue.add(newData(6).setPriority(LOWEST));
    queue.add(newData(7).setPriority(LOW));
    queue.add(newData(1).setPriority(HIGHEST));
    queue.add(newData(5));
    queue.add(newData(3).setPriority(HIGH));
    AtomicBoolean emptyWaiterDone = new AtomicBoolean();
    createThread(emptyWaiter(emptyWaiterDone)).start();
    AtomicInteger consumerCount = new AtomicInteger();
    Thread consumer = createThread(consumer(consumerCount, true));

    assertFalse(emptyWaiterDone.get());
    consumer.start();
    awaitNotRunning(consumer);
    assertTrue(queue.isEmpty());
    assertTrue(emptyWaiterDone.get());
    assertEquals(10, consumerCount.get());

    queue.add(newData(10));
    queue.add(newData(11));
    queue.add(newData(12));
    queue.add(newData(13));
    queue.add(newData(14));
    awaitNotRunning(consumer);
    assertTrue(queue.isEmpty());
    assertEquals(15, consumerCount.get());
  }

  @Test
  public void test_auto() throws Exception {
    AtomicInteger consumerCount = new AtomicInteger();
    Thread consumer = createThread(consumer(consumerCount, true));
    consumer.start();
    int producerCount = 10;
    int nbPer = 10;
    for (int i = 0; i < producerCount; i++) {
      int from = i * nbPer;
      createThread(producer(i, from, from + nbPer)).start();
    }
    awaitNotRunning(consumer);
    assertTrue(queue.isEmpty());
    assertEquals(producerCount * nbPer, consumerCount.get());
  }

  private Thread createThread(Runnable run) {
    threads.add(new Thread(run));
    return threads.get(threads.size() - 1);
  }

  private Runnable producer(final int threadNb, int from, int to) {
    return () -> {
      try {
        Random rand = new Random();
        for (int i = from; i < to && running.get(); i++) {
          IndexData data = newData(i).setPriority(new IndexQueuePriority(rand.nextInt()));
          queue.put(data);
          System.out.println(threadNb + " produced: " + data.getWiki());
        }
      } catch (InterruptedException exc) {
        Thread.currentThread().interrupt();
      }
    };
  }

  private Runnable consumer(AtomicInteger count, boolean strict) {
    return () -> {
      try {
        while (running.get()) {
          IndexData data = queue.take();
          assertNotNull(data);
          System.out.println("consume: " + data.getWiki());
          if (strict) {
            assertEquals(count.getAndIncrement() + "", data.getWiki());
          } else {
            count.incrementAndGet();
          }
          Thread.sleep(1);
        }
      } catch (InterruptedException exc) {
        Thread.currentThread().interrupt();
      }
    };
  }

  private Runnable emptyWaiter(AtomicBoolean done) {
    return () -> {
      try {
        queue.awaitEmpty();
        done.set(true);
      } catch (InterruptedException exc) {
        Thread.currentThread().interrupt();
      }
    };
  }

  private static void awaitNotRunning(Thread thread) throws InterruptedException {
    long timeOut = System.currentTimeMillis() + 10000;
    while (thread.getState() == State.RUNNABLE) {
      assertTrue("waiting timed out", System.currentTimeMillis() < timeOut);
      Thread.sleep(1);
    }
  }

  private static IndexData newData(Integer nb) {
    return new WikiData(new WikiReference(Integer.toString(nb)), false);
  }
}
