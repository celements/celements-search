package com.celements.search.lucene.index.queue;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.plugin.lucene.DeleteData;
import com.xpn.xwiki.web.Utils;

public class LuceneIndexingPriorityQueueTest extends AbstractComponentTest {

  private LuceneIndexingQueue queue;

  @Before
  public void prepareTest() {
    queue = Utils.getComponent(LuceneIndexingQueue.class, LuceneIndexingPriorityQueue.NAME);
  }

  @Test
  public void test_empty() {
    assertTrue(queue.isEmpty());
    queue.add(new DeleteData("1"));
    assertFalse(queue.isEmpty());
    queue.remove();
    assertTrue(queue.isEmpty());
  }

  @Test
  public void test_size() {
    assertEquals(0, queue.getSize());
    queue.add(new DeleteData("1"));
    assertEquals(1, queue.getSize());
    queue.add(new DeleteData("2"));
    assertEquals(2, queue.getSize());
    queue.remove();
    assertEquals(1, queue.getSize());
    queue.remove();
    assertEquals(0, queue.getSize());
  }

  @Test
  public void test_contains() {
    String id = "asdf";
    assertFalse(queue.contains(id));
    queue.add(new DeleteData(id));
    assertTrue(queue.contains(id));
    queue.remove();
    assertFalse(queue.contains(id));
  }

  @Test
  public void test_fifo() {
    queue.add(new DeleteData("1"));
    queue.add(new DeleteData("2"));
    queue.add(new DeleteData("3"));
    assertEquals("1", queue.remove().getId());
    assertEquals("2", queue.remove().getId());
    assertEquals("3", queue.remove().getId());
  }

  @Test
  public void test_prio() {
    queue.add(new DeleteData("low1").setPriority(IndexQueuePriority.LOW));
    queue.add(new DeleteData("high1").setPriority(IndexQueuePriority.HIGH));
    queue.add(new DeleteData("low2").setPriority(IndexQueuePriority.LOW));
    queue.add(new DeleteData("default1"));
    queue.add(new DeleteData("default2"));
    queue.add(new DeleteData("high2").setPriority(IndexQueuePriority.HIGH));
    assertEquals("high1", queue.remove().getId());
    assertEquals("high2", queue.remove().getId());
    assertEquals("default1", queue.remove().getId());
    assertEquals("default2", queue.remove().getId());
    assertEquals("low1", queue.remove().getId());
    assertEquals("low2", queue.remove().getId());
  }

}
