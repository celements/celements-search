package com.celements.search.lucene.index.queue;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.index.LuceneDocId;
import com.celements.search.lucene.index.WikiData;
import com.xpn.xwiki.web.Utils;

public class LuceneIndexingPriorityQueueTest extends AbstractComponentTest {

  private LuceneIndexingPriorityQueue queue;

  @Before
  public void prepareTest() {
    queue = (LuceneIndexingPriorityQueue) Utils.getComponent(LuceneIndexingQueue.class,
        LuceneIndexingPriorityQueue.NAME);
  }

  @Test
  public void test_empty() {
    assertTrue(queue.isEmpty());
    queue.add(newData("1"));
    assertFalse(queue.isEmpty());
    queue.remove();
    assertTrue(queue.isEmpty());
  }

  @Test
  public void test_size() {
    assertEquals(0, queue.getSize());
    queue.add(newData("1"));
    assertEquals(1, queue.getSize());
    queue.add(newData("2"));
    assertEquals(2, queue.getSize());
    queue.remove();
    assertEquals(1, queue.getSize());
    queue.remove();
    assertEquals(0, queue.getSize());
  }

  @Test
  public void test_contains() {
    String id = "asdf";
    assertFalse(queue.contains(getId(id)));
    queue.add(newData(id));
    assertTrue(queue.contains(getId(id)));
    queue.remove();
    assertFalse(queue.contains(getId(id)));
  }

  @Test
  public void test_fifo() {
    queue.add(newData("1"));
    queue.add(newData("2"));
    queue.add(newData("3"));
    assertEquals("1", queue.remove().getId().serialize());
    assertEquals("2", queue.remove().getId().serialize());
    assertEquals("3", queue.remove().getId().serialize());
  }

  @Test
  public void test_prio() {
    queue.add(newData("low1").setPriority(IndexQueuePriority.LOW));
    queue.add(newData("high1").setPriority(IndexQueuePriority.HIGH));
    queue.add(newData("low2").setPriority(IndexQueuePriority.LOW));
    queue.add(newData("default1"));
    queue.add(newData("default2"));
    queue.add(newData("high2").setPriority(IndexQueuePriority.HIGH));
    assertEquals("high1", queue.remove().getId().serialize());
    assertEquals("high2", queue.remove().getId().serialize());
    assertEquals("default1", queue.remove().getId().serialize());
    assertEquals("default2", queue.remove().getId().serialize());
    assertEquals("low1", queue.remove().getId().serialize());
    assertEquals("low2", queue.remove().getId().serialize());
  }

  private static LuceneDocId getId(String name) {
    return new LuceneDocId(new WikiReference(name), null);
  }

  private static WikiData newData(String name) {
    return new WikiData(new WikiReference(name));
  }

}
