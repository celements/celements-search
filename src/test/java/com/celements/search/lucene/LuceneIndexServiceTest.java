package com.celements.search.lucene;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ImmutableDocumentReference;
import org.xwiki.observation.ObservationManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.observation.LuceneQueueEvent;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.xpn.xwiki.web.Utils;

public class LuceneIndexServiceTest extends AbstractComponentTest {

  private LuceneIndexService service;
  private EntityReference ref;
  private ObservationManager observationManagerMock;

  @Before
  public void prepareTest() throws Exception {
    observationManagerMock = registerComponentMock(ObservationManager.class);
    service = (LuceneIndexService) Utils.getComponent(ILuceneIndexService.class);
    ref = new ImmutableDocumentReference("wiki", "space", "doc");
  }

  @Test
  public void test_queue() {
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueIndexEvent.class), same(ref), capture(data));
    replayDefault();
    service.queue(ref);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(IndexQueuePriority.DEFAULT, false), data.getValue());
  }

  @Test
  public void test_queue_fromExec() {
    IndexQueuePriority prio = IndexQueuePriority.HIGH;
    setExecParam(LuceneIndexService.EXEC_QUEUE_PRIORITY, prio);
    boolean disableEventNotification = true;
    setExecParam(LuceneIndexService.EXEC_DISABLE_EVENT_NOTIFICATION, disableEventNotification);
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueIndexEvent.class), same(ref), capture(data));
    replayDefault();
    service.queue(ref);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(prio, disableEventNotification), data.getValue());
  }

  @Test
  public void test_queue_prio() {
    IndexQueuePriority prio = IndexQueuePriority.HIGH;
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueIndexEvent.class), same(ref), capture(data));
    replayDefault();
    service.queue(ref, prio);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(prio, false), data.getValue());
  }

  @Test
  public void test_queue_prio_fromExec() {
    IndexQueuePriority prio = IndexQueuePriority.HIGH;
    setExecParam(LuceneIndexService.EXEC_QUEUE_PRIORITY, IndexQueuePriority.LOW);
    boolean disableEventNotification = true;
    setExecParam(LuceneIndexService.EXEC_DISABLE_EVENT_NOTIFICATION, disableEventNotification);
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueIndexEvent.class), same(ref), capture(data));
    replayDefault();
    service.queue(ref, prio);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(prio, disableEventNotification), data.getValue());
  }

  @Test
  public void test_queue_withoutNotifications() {
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueIndexEvent.class), same(ref), capture(data));
    replayDefault();
    service.queue(ref, null, true);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(IndexQueuePriority.DEFAULT, true), data.getValue());
  }

  @Test
  public void test_queue_withoutNotifications_fromExec() {
    IndexQueuePriority prio = IndexQueuePriority.HIGH;
    setExecParam(LuceneIndexService.EXEC_QUEUE_PRIORITY, prio);
    setExecParam(LuceneIndexService.EXEC_DISABLE_EVENT_NOTIFICATION, false);
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueIndexEvent.class), same(ref), capture(data));
    replayDefault();
    service.queue(ref, null, true);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(prio, true), data.getValue());
  }

  @Test
  public void test_queue_withoutNotifications_prio() {
    IndexQueuePriority prio = IndexQueuePriority.HIGH;
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueIndexEvent.class), same(ref), capture(data));
    replayDefault();
    service.queue(ref, prio, true);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(prio, true), data.getValue());
  }

  @Test
  public void test_queue_withoutNotifications_prio_fromExec() {
    IndexQueuePriority prio = IndexQueuePriority.HIGH;
    setExecParam(LuceneIndexService.EXEC_QUEUE_PRIORITY, IndexQueuePriority.LOW);
    setExecParam(LuceneIndexService.EXEC_DISABLE_EVENT_NOTIFICATION, false);
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueIndexEvent.class), same(ref), capture(data));
    replayDefault();
    service.queue(ref, prio, true);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(prio, true), data.getValue());
  }

  @Test
  public void test_queueDelete() {
    IndexQueuePriority prio = IndexQueuePriority.HIGH;
    Capture<LuceneQueueEvent.Data> data = EasyMock.newCapture();
    observationManagerMock.notify(isA(LuceneQueueDeleteEvent.class), same(ref), capture(data));
    replayDefault();
    service.queueDelete(ref, prio, true);
    verifyDefault();
    assertEquals(new LuceneQueueEvent.Data(prio, true), data.getValue());
  }

  private void setExecParam(String key, Object value) {
    Utils.getComponent(Execution.class).getContext().setProperty(key, value);
  }

}
