package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.index.LuceneDocId;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteLocalEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexLocalEvent;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.AttachmentAddedEvent;
import com.xpn.xwiki.internal.event.AttachmentDeletedEvent;
import com.xpn.xwiki.internal.event.AttachmentUpdatedEvent;
import com.xpn.xwiki.web.Utils;

public class QueueAttachmentEventConverterTest extends AbstractComponentTest {

  QueueAttachmentEventConverter listener;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(ObservationManager.class);
    listener = (QueueAttachmentEventConverter) Utils.getComponent(EventListener.class,
        QueueAttachmentEventConverter.NAME);
  }

  @Test
  public void test_remote() {
    assertTrue("needs to listen on remote events", listener instanceof AbstractRemoteEventListener);
  }

  @Test
  public void test_getEvents() {
    assertEquals(3, listener.getEvents().size());
    assertSame(AttachmentAddedEvent.class, listener.getEvents().get(0).getClass());
    assertSame(AttachmentUpdatedEvent.class, listener.getEvents().get(1).getClass());
    assertSame(AttachmentDeletedEvent.class, listener.getEvents().get(2).getClass());
  }

  @Test
  public void test_onEvent_created() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    getMock(ObservationManager.class).notify(isA(LuceneQueueIndexLocalEvent.class),
        eq(new LuceneDocId(attRef)), isNull());

    replayDefault();
    listener.onEvent(new AttachmentAddedEvent("docName", attRef.getName()), doc, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_updated() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    getMock(ObservationManager.class).notify(isA(LuceneQueueIndexLocalEvent.class),
        eq(new LuceneDocId(attRef)), isNull());

    replayDefault();
    listener.onEvent(new AttachmentUpdatedEvent("docName", attRef.getName()), doc, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_deleted() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    getMock(ObservationManager.class).notify(isA(LuceneQueueDeleteLocalEvent.class),
        eq(new LuceneDocId(attRef)), isNull());

    replayDefault();
    listener.onEvent(new AttachmentDeletedEvent("docName", attRef.getName()), doc, null);
    verifyDefault();
  }

}
