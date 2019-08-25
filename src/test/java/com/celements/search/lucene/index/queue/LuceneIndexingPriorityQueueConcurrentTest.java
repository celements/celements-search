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

import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.index.IndexData;
import com.celements.search.lucene.index.WikiData;
import com.xpn.xwiki.web.Utils;

public class LuceneIndexingPriorityQueueConcurrentTest extends AbstractComponentTest {

  private LuceneIndexingPriorityQueue queue;
  private Consumer consumer;
  private List<Thread> threads = new ArrayList<>();
  private AtomicBoolean running = new AtomicBoolean();

  @Before
  public void prepare() {
    queue = (LuceneIndexingPriorityQueue) Utils.getComponent(LuceneIndexingQueue.class,
        LuceneIndexingPriorityQueue.NAME);
    consumer = new Consumer();
    running.set(true);
  }

  @After
  public void cleanup() {
    running.set(false);
    consumer.interrupt();
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

    assertFalse(emptyWaiterDone.get());
    consumer.start();
    awaitConsumerFinished();
    assertTrue(queue.isEmpty());
    assertTrue(emptyWaiterDone.get());
    assertEquals(10, consumer.count.get());

    queue.add(newData(10));
    queue.add(newData(11));
    queue.add(newData(12));
    queue.add(newData(13));
    queue.add(newData(14));
    awaitConsumerFinished();
    assertTrue(queue.isEmpty());
    assertEquals(15, consumer.count.get());
    assertFalse(consumer.failed.get());
  }

  @Test
  public void test_auto() throws Exception {
    consumer.strict = false;
    for (int i = 1000; i < 2000; i++) {
      queue.add(newData(i).setPriority(HIGHEST));
    }
    int producerCount = 10;
    int nbPer = 100;
    for (int i = 0; i < producerCount; i++) {
      int from = i * nbPer;
      createThread(producer(i, from, from + nbPer)).start();
      Thread.sleep(1);
    }
    assertEquals(1000, queue.getSize());
    threads.forEach(t -> assertSame(State.WAITING, t.getState()));
    consumer.start();
    awaitConsumerFinished();
    assertTrue(queue.isEmpty());
    assertEquals((producerCount * nbPer) + 1000, consumer.count.get());
    assertFalse(consumer.failed.get());
  }

  private Thread createThread(Runnable run) {
    threads.add(new Thread(run));
    return threads.get(threads.size() - 1);
  }

  private class Consumer extends Thread {

    final AtomicBoolean failed = new AtomicBoolean();
    final AtomicInteger count = new AtomicInteger();
    boolean strict = true;

    @Override
    public void run() {
      try {
        while (running.get()) {
          IndexData data = queue.take();
          if (strict && (count.get() != Integer.parseInt(data.getWikiRef().getName()))) {
            failed.set(true);
          }
          count.incrementAndGet();
          // System.out.println("consumed: " + data);
        }
      } catch (InterruptedException exc) {
        failed.set(true);
        Thread.currentThread().interrupt();
      }
    }
  }

  private Runnable producer(final int threadNb, int from, int to) {
    return () -> {
      try {
        Random rand = new Random();
        for (int i = from; (i < to) && running.get(); i++) {
          IndexData data = newData(i).setPriority(new IndexQueuePriority(rand.nextInt()));
          queue.put(data);
          // System.out.println(threadNb + " produced: " + data);
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

  private void awaitConsumerFinished() throws InterruptedException {
    long timeOut = System.currentTimeMillis() + 10000;
    while (!queue.isEmpty() || (consumer.getState() == State.RUNNABLE)) {
      assertTrue("waiting timed out", System.currentTimeMillis() < timeOut);
      Thread.sleep(1);
    }
  }

  private static IndexData newData(Integer nb) {
    return new WikiData(new WikiReference(Integer.toString(nb)));
  }
}
