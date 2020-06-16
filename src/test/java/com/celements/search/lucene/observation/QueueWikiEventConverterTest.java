package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;

import com.celements.common.observation.listener.AbstractEventListener;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.ILuceneIndexService;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.index.queue.QueueTask;
import com.xpn.xwiki.web.Utils;

public class QueueWikiEventConverterTest extends AbstractComponentTest {

  private QueueWikiEventConverter listener;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(ILuceneIndexService.class);
    listener = (QueueWikiEventConverter) Utils.getComponent(EventListener.class,
        QueueWikiEventConverter.NAME);
  }

  @Test
  public void test_remote() {
    assertTrue("needs to listen on local events only, LuceneQueueEvents are distributed remotely",
        listener instanceof AbstractEventListener);
  }

  @Test
  public void test_getEvents() {
    assertEquals(2, listener.getEvents().size());
    assertSame(WikiCreatedEvent.class, listener.getEvents().get(0).getClass());
    assertSame(WikiDeletedEvent.class, listener.getEvents().get(1).getClass());
  }

  @Test
  public void test_onEvent_created() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    expect(getMock(ILuceneIndexService.class).indexTask(wikiRef)).andReturn(createQueueTaskMock());

    replayDefault();
    listener.onEvent(new WikiCreatedEvent(wikiRef.getName()), wikiRef, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_deleted() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    expect(getMock(ILuceneIndexService.class).deleteTask(wikiRef)).andReturn(createQueueTaskMock());

    replayDefault();
    listener.onEvent(new WikiDeletedEvent(wikiRef.getName()), wikiRef, null);
    verifyDefault();
  }

  private QueueTask createQueueTaskMock() {
    QueueTask mock = createMockAndAddToDefault(QueueTask.class);
    expect(mock.priority(IndexQueuePriority.LOW)).andReturn(mock);
    mock.queue();
    return mock;
  }

}
