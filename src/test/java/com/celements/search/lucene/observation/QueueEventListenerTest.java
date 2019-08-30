package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.rendering.syntax.Syntax;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.search.lucene.index.AttachmentData;
import com.celements.search.lucene.index.DeleteData;
import com.celements.search.lucene.index.DocumentData;
import com.celements.search.lucene.index.IndexData;
import com.celements.search.lucene.index.LuceneDocId;
import com.celements.search.lucene.index.WikiData;
import com.celements.search.lucene.index.queue.LuceneIndexingQueue;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteLocalEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexLocalEvent;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class QueueEventListenerTest extends AbstractComponentTest {

  QueueEventListener listener;
  IModelAccessFacade modelAccessMock;

  @Before
  public void prepareTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    listener = (QueueEventListener) Utils.getComponent(EventListener.class,
        QueueEventListener.NAME);
  }

  @Test
  public void test_remote() {
    assertTrue("needs to listen on remote events", listener instanceof AbstractRemoteEventListener);
  }

  @Test
  public void test_getEvents() {
    assertEquals(4, listener.getEvents().size());
    assertSame(LuceneQueueIndexEvent.class, listener.getEvents().get(0).getClass());
    assertSame(LuceneQueueDeleteEvent.class, listener.getEvents().get(1).getClass());
    assertSame(LuceneQueueIndexLocalEvent.class, listener.getEvents().get(2).getClass());
    assertSame(LuceneQueueDeleteLocalEvent.class, listener.getEvents().get(3).getClass());
  }

  @Test
  public void test_onEvent_docRef() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    createDocMock(docRef);

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), docRef, null);
    verifyDefault();

    assertEquals(1, getQueue().getSize());
    IndexData data = getQueue().remove();
    assertEquals(DocumentData.class, data.getClass());
    assertEquals(new LuceneDocId(docRef), data.getId());
  }

  @Test
  public void test_onEvent_attRef() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    XWikiDocument doc = createDocMock(docRef);
    XWikiAttachment att = createMockAndAddToDefault(XWikiAttachment.class);
    expect(att.getDoc()).andReturn(doc).atLeastOnce();
    expect(att.getFilename()).andReturn(attRef.getName()).atLeastOnce();
    expect(att.getDate()).andReturn(new Date());
    expect(att.getAuthor()).andReturn("heinrich");
    expect(att.getFilesize()).andReturn(5);
    expect(att.getMimeType(getContext())).andReturn("mime");
    expect(modelAccessMock.getAttachmentNameEqual(same(doc), eq(attRef.getName())))
        .andReturn(att);

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), new LuceneDocId(attRef), null);
    verifyDefault();

    assertEquals(1, getQueue().getSize());
    IndexData data = getQueue().remove();
    assertEquals(AttachmentData.class, data.getClass());
    assertEquals(new LuceneDocId(attRef), data.getId());
  }

  @Test
  public void test_onEvent_wikiRef() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), new LuceneDocId(wikiRef), null);
    verifyDefault();

    assertEquals(1, getQueue().getSize());
    IndexData data = getQueue().remove();
    assertEquals(WikiData.class, data.getClass());
    assertEquals(new LuceneDocId(wikiRef), data.getId());
  }

  @Test
  public void test_onEvent_delete() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");

    replayDefault();
    listener.onEvent(new LuceneQueueDeleteEvent(), new LuceneDocId(wikiRef), null);
    verifyDefault();

    assertEquals(1, getQueue().getSize());
    IndexData data = getQueue().remove();
    assertEquals(DeleteData.class, data.getClass());
    assertEquals(new LuceneDocId(wikiRef), data.getId());
  }

  @Test
  public void test_onEvent_otherRef() throws Exception {
    SpaceReference ref = new SpaceReference("space", new WikiReference("wiki"));

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), new LuceneDocId(ref), null);
    verifyDefault();

    assertEquals(0, getQueue().getSize());
  }

  @Test
  public void test_onEvent_DocumentNotExistsException() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    expect(modelAccessMock.getDocument(docRef, "default"))
        .andThrow(new DocumentNotExistsException(docRef));

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), new LuceneDocId(docRef), null);
    verifyDefault();

    assertEquals(0, getQueue().getSize());
  }

  @Test
  public void test_onEvent_AttachmentNotExistsException() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    XWikiDocument doc = createDocMock(docRef);
    expect(modelAccessMock.getAttachmentNameEqual(same(doc), eq(attRef.getName())))
        .andThrow(new AttachmentNotExistsException(attRef));

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), new LuceneDocId(attRef), null);
    verifyDefault();

    assertEquals(0, getQueue().getSize());
  }

  private XWikiDocument createDocMock(DocumentReference docRef) throws Exception {
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(doc.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(doc.getVersion()).andReturn("1.0").anyTimes();
    expect(doc.getLanguage()).andReturn("").anyTimes();
    expect(doc.getAuthor()).andReturn("köbi").anyTimes();
    expect(doc.getCreator()).andReturn("chnobli").anyTimes();
    expect(doc.getDate()).andReturn(new Date()).anyTimes();
    expect(doc.getCreationDate()).andReturn(new Date()).anyTimes();
    expect(doc.getRenderedTitle(anyObject(Syntax.class), same(getContext())))
        .andReturn("title").anyTimes();
    expect(modelAccessMock.getDocument(docRef)).andReturn(doc);
    return doc;
  }

  private static LuceneIndexingQueue getQueue() {
    return Utils.getComponent(LuceneIndexingQueue.class);
  }

}
