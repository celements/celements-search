package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.AbstractIndexData;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

public class QueueEventListenerTest extends AbstractComponentTest {

  private TestQueueEventListener listener;
  private XWikiDocument doc;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    listener = (TestQueueEventListener) Utils.getComponent(EventListener.class,
        QueueEventListener.NAME);
    expect(getWikiMock().getPlugin(eq("lucene"), same(getContext())))
        .andReturn(createMockAndAddToDefault(LucenePlugin.class)).anyTimes();
    doc = new XWikiDocument(new ImmutableDocumentReference("wiki", "space", "doc"));
    expect(getWikiMock().getDocument(doc.getDocumentReference(), getContext()))
        .andReturn(doc).anyTimes();
  }

  @Test
  public void test_remote() {
    assertTrue("needs to listen on remote events", listener instanceof AbstractRemoteEventListener);
  }

  @Test
  public void test_getEvents() {
    assertEquals(2, listener.getEvents().size());
    assertSame(LuceneQueueIndexEvent.class, listener.getEvents().get(0).getClass());
    assertSame(LuceneQueueDeleteEvent.class, listener.getEvents().get(1).getClass());
  }

  @Test
  public void test_onEvent_docRef() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    expectIndexData(listener.docDataMock, LuceneQueueEvent.Data.DEFAULT);

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), doc.getDocumentReference(), null);
    verifyDefault();

    assertEquals(1, listener.allCapturedSize());
    assertEquals(1, listener.docsIndex.size());
    assertEquals(doc, listener.docsIndex.get(0));
  }

  @Test
  public void test_onEvent_docRef_otherEventData() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    LuceneQueueEvent.Data data = new LuceneQueueEvent.Data(IndexQueuePriority.LOW, true);
    expectIndexData(listener.docDataMock, data);

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), doc.getDocumentReference(), data);
    verifyDefault();

    assertEquals(1, listener.allCapturedSize());
    assertEquals(1, listener.docsIndex.size());
    assertEquals(doc, listener.docsIndex.get(0));
  }

  @Test
  public void test_onEvent_docRef_withAtts() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    expectIndexData(listener.docDataMock, LuceneQueueEvent.Data.DEFAULT);
    XWikiAttachment att = new XWikiAttachment(doc, "file");
    doc.setAttachmentList(ImmutableList.of(att));
    expectIndexData(listener.attDataMock, LuceneQueueEvent.Data.DEFAULT);

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), doc.getDocumentReference(), null);
    verifyDefault();

    assertEquals(2, listener.allCapturedSize());
    assertEquals(1, listener.docsIndex.size());
    assertEquals(doc, listener.docsIndex.get(0));
    assertEquals(1, listener.attsIndex.size());
    assertEquals(att, listener.attsIndex.get(0));
  }

  @Test
  public void test_onEvent_docRef_delete() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    expectIndexData(listener.docDataMock, LuceneQueueEvent.Data.DEFAULT);
    XWikiAttachment att = new XWikiAttachment(doc, "file");
    doc.setAttachmentList(ImmutableList.of(att));
    expectIndexData(listener.attDataMock, LuceneQueueEvent.Data.DEFAULT);

    replayDefault();
    listener.onEvent(new LuceneQueueDeleteEvent(), doc.getDocumentReference(), null);
    verifyDefault();

    assertEquals(2, listener.allCapturedSize());
    assertEquals(1, listener.docsDelete.size());
    assertEquals(doc, listener.docsDelete.get(0));
    assertEquals(1, listener.attsDelete.size());
    assertEquals(att, listener.attsDelete.get(0));
  }

  @Test
  public void test_onEvent_attRef_otherEventData() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    AttachmentReference attRef = new AttachmentReference("file", doc.getDocumentReference());
    XWikiAttachment att = new XWikiAttachment(doc, attRef.getName());
    doc.setAttachmentList(ImmutableList.of(att));
    LuceneQueueEvent.Data data = new LuceneQueueEvent.Data(IndexQueuePriority.LOW, true);
    expectIndexData(listener.attDataMock, data);

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), attRef, data);
    verifyDefault();

    assertEquals(1, listener.allCapturedSize());
    assertEquals(1, listener.attsIndex.size());
    assertEquals(att, listener.attsIndex.get(0));
  }

  @Test
  public void test_onEvent_attRef() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    AttachmentReference attRef = new AttachmentReference("file", doc.getDocumentReference());
    XWikiAttachment att = new XWikiAttachment(doc, attRef.getName());
    doc.setAttachmentList(ImmutableList.of(att));
    expectIndexData(listener.attDataMock, LuceneQueueEvent.Data.DEFAULT);

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), attRef, null);
    verifyDefault();

    assertEquals(1, listener.allCapturedSize());
    assertEquals(1, listener.attsIndex.size());
    assertEquals(att, listener.attsIndex.get(0));
  }

  @Test
  public void test_onEvent_attRef_delete() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    AttachmentReference attRef = new AttachmentReference("file", doc.getDocumentReference());
    XWikiAttachment att = new XWikiAttachment(doc, attRef.getName());
    doc.setAttachmentList(ImmutableList.of(att));
    expectIndexData(listener.attDataMock, LuceneQueueEvent.Data.DEFAULT);

    replayDefault();
    listener.onEvent(new LuceneQueueDeleteEvent(), attRef, null);
    verifyDefault();

    assertEquals(1, listener.allCapturedSize());
    assertEquals(1, listener.attsDelete.size());
    assertEquals(att, listener.attsDelete.get(0));
  }

  @Test
  public void test_onEvent_wikiRef() throws Exception {
    WikiReference wikiRef = doc.getDocumentReference().getWikiReference();
    expectIndexData(listener.wikiDataMock, LuceneQueueEvent.Data.DEFAULT);

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), wikiRef, null);
    verifyDefault();

    assertEquals(1, listener.allCapturedSize());
    assertEquals(1, listener.wikisIndex.size());
    assertEquals(wikiRef, listener.wikisIndex.get(0));
  }

  @Test
  public void test_onEvent_wikiRef_delete() throws Exception {
    WikiReference wikiRef = doc.getDocumentReference().getWikiReference();
    expectIndexData(listener.wikiDataMock, LuceneQueueEvent.Data.DEFAULT);

    replayDefault();
    listener.onEvent(new LuceneQueueDeleteEvent(), wikiRef, null);
    verifyDefault();

    assertEquals(1, listener.allCapturedSize());
    assertEquals(1, listener.wikisDelete.size());
    assertEquals(wikiRef, listener.wikisDelete.get(0));
  }

  private void expectIndexData(AbstractIndexData indexData, LuceneQueueEvent.Data data) {
    getMock(LucenePlugin.class).queue(same(indexData));
    expect(indexData.setPriority(data.priority)).andReturn(indexData);
    expect(indexData.disableObservationEventNotification(data.disableEventNotification))
        .andReturn(indexData);
  }

  @Test
  public void test_onEvent_otherRef() throws Exception {
    SpaceReference ref = new SpaceReference("space", new WikiReference("wiki"));

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), ref, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_DNE() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andThrow(new DocumentNotExistsException(doc.getDocumentReference()));

    replayDefault();
    listener.onEvent(new LuceneQueueIndexEvent(), doc.getDocumentReference(), null);
    verifyDefault();
  }

}
