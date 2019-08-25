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
import org.xwiki.observation.ObservationManager;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteLocalEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexLocalEvent;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class QueueDocumentEventConverterTest extends AbstractComponentTest {

  QueueDocumentEventConverter listener;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(ObservationManager.class);
    listener = (QueueDocumentEventConverter) Utils.getComponent(EventListener.class,
        QueueDocumentEventConverter.NAME);
  }

  @Test
  public void test_remote() {
    assertTrue("needs to listen on remote events", listener instanceof AbstractRemoteEventListener);
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
    getMock(ObservationManager.class).notify(isA(LuceneQueueIndexLocalEvent.class),
        eq(docRef), isNull());

    replayDefault();
    listener.onEvent(new DocumentCreatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_updated() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    getMock(ObservationManager.class).notify(isA(LuceneQueueIndexLocalEvent.class),
        eq(docRef), isNull());

    replayDefault();
    listener.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_deleted() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    getMock(ObservationManager.class).notify(isA(LuceneQueueDeleteLocalEvent.class),
        eq(docRef), isNull());

    replayDefault();
    listener.onEvent(new DocumentDeletedEvent(), doc, null);
    verifyDefault();
  }

}
