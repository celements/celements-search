package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;

import com.celements.common.observation.listener.AbstractEventListener;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.ILuceneIndexService;
import com.celements.search.lucene.QueueTask;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class QueueDocumentEventConverterTest extends AbstractComponentTest {

  private QueueDocumentEventConverter listener;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(ILuceneIndexService.class);
    listener = (QueueDocumentEventConverter) Utils.getComponent(EventListener.class,
        QueueDocumentEventConverter.NAME);
  }

  @Test
  public void test_remote() {
    assertTrue("needs to listen on local events only, LuceneQueueEvents are distributed remotely",
        listener instanceof AbstractEventListener);
  }

  @Test
  public void test_getEvents() {
    assertEquals(3, listener.getEvents().size());
    assertSame(DocumentCreatedEvent.class, listener.getEvents().get(0).getClass());
    assertSame(DocumentUpdatedEvent.class, listener.getEvents().get(1).getClass());
    assertSame(DocumentDeletedEvent.class, listener.getEvents().get(2).getClass());
  }

  @Test
  public void test_onEvent_created() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(ILuceneIndexService.class).indexTask(docRef)).andReturn(createQueueTaskMock());

    replayDefault();
    listener.onEvent(new DocumentCreatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_updated() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(ILuceneIndexService.class).indexTask(docRef)).andReturn(createQueueTaskMock());

    replayDefault();
    listener.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_deleted() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(ILuceneIndexService.class).deleteTask(docRef)).andReturn(createQueueTaskMock());

    replayDefault();
    listener.onEvent(new DocumentDeletedEvent(), doc, null);
    verifyDefault();
  }

  private QueueTask createQueueTaskMock() {
    QueueTask mock = createMockAndAddToDefault(QueueTask.class);
    expect(mock.priority(isNull())).andReturn(mock);
    mock.queue();
    return mock;
  }

}
