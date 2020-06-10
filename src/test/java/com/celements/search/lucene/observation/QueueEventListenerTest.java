package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.search.lucene.observation.QueueEventListener.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

public class QueueEventListenerTest extends AbstractComponentTest {

  QueueEventListener listener;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    listener = (QueueEventListener) Utils.getComponent(EventListener.class,
        QueueEventListener.NAME);
    expect(getWikiMock().getPlugin(eq("lucene"), same(getContext())))
        .andReturn(createMockAndAddToDefault(LucenePlugin.class)).anyTimes();
  }

  @Test
  public void test_remote() {
    assertTrue("needs to listen on remote events", listener instanceof AbstractRemoteEventListener);
  }

  @Test
  public void test_getEvents() {
    assertEquals(1, listener.getEvents().size());
    assertSame(LuceneQueueEvent.class, listener.getEvents().get(0).getClass());
  }

  @Test
  public void test_onEvent_docRef() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(IModelAccessFacade.class).getDocument(docRef)).andReturn(doc);
    getMock(LucenePlugin.class).queueDocument(same(doc), same(getContext()));
    getMock(LucenePlugin.class).queueAttachment(same(doc), same(getContext()));

    replayDefault();
    listener.onEvent(new LuceneQueueEvent(), docRef, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_attRef() throws Exception {
    XWikiDocument doc = new XWikiDocument(new DocumentReference("wiki", "space", "doc"));
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    AttachmentReference attRef = new AttachmentReference("file", doc.getDocumentReference());
    XWikiAttachment att = new XWikiAttachment(doc, attRef.getName());
    doc.setAttachmentList(ImmutableList.of(att));
    getMock(LucenePlugin.class).queueAttachment(same(doc), same(att), same(getContext()));

    replayDefault();
    listener.onEvent(new LuceneQueueEvent(), attRef, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_otherRef() throws Exception {
    SpaceReference ref = new SpaceReference("space", new WikiReference("wiki"));

    replayDefault();
    listener.onEvent(new LuceneQueueEvent(), ref, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_DNE() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    expect(getMock(IModelAccessFacade.class).getDocument(docRef))
        .andThrow(new DocumentNotExistsException(docRef));

    replayDefault();
    listener.onEvent(new LuceneQueueEvent(), docRef, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_eventNotification_enabled() throws Exception {
    ExecutionContext ctx = Utils.getComponent(Execution.class).getContext();
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    getMock(LucenePlugin.class).queueDocument(same(doc), same(getContext()));
    getMock(LucenePlugin.class).queueAttachment(same(doc), same(getContext()));
    expect(getMock(IModelAccessFacade.class).getDocument(docRef))
        .andAnswer(() -> {
          assertEquals(false, ctx.getProperty(KEY_DISABLE_EVENTS));
          return doc;
        });

    replayDefault();
    listener.onEvent(new LuceneQueueEvent(), docRef, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_eventNotification_disabled() throws Exception {
    ExecutionContext ctx = Utils.getComponent(Execution.class).getContext();
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    getMock(LucenePlugin.class).queueDocument(same(doc), same(getContext()));
    getMock(LucenePlugin.class).queueAttachment(same(doc), same(getContext()));
    expect(getMock(IModelAccessFacade.class).getDocument(docRef))
        .andAnswer(() -> {
          assertEquals(true, ctx.getProperty(KEY_DISABLE_EVENTS));
          return doc;
        });

    ctx.setProperty(KEY_DISABLE_EVENTS, "KeepMe");
    replayDefault();
    listener.onEvent(new LuceneQueueEvent(), docRef, true);
    verifyDefault();
    assertEquals("KeepMe", ctx.getProperty(KEY_DISABLE_EVENTS));
  }

}
