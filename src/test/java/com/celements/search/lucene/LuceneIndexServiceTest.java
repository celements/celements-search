package com.celements.search.lucene;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.ObservationManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.xpn.xwiki.web.Utils;

public class LuceneIndexServiceTest extends AbstractComponentTest {

  LuceneIndexService service;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(ObservationManager.class);
    service = (LuceneIndexService) Utils.getComponent(ILuceneIndexService.class);
  }

  @Test
  public void test_queue() {
    Capture<LuceneQueueIndexEvent> eventCapture = EasyMock.newCapture();
    EntityReference ref = new DocumentReference("wiki", "space", "doc");
    getMock(ObservationManager.class).notify(capture(eventCapture), same(ref), isNull());
    replayDefault();
    service.queue(ref);
    verifyDefault();
    assertNotNull(eventCapture.getValue());
    assertFalse(eventCapture.getValue().getPriority().isPresent());
  }

  @Test
  public void test_queue_prio() {
    Capture<LuceneQueueIndexEvent> eventCapture = EasyMock.newCapture();
    EntityReference ref = new DocumentReference("wiki", "space", "doc");
    getMock(ObservationManager.class).notify(capture(eventCapture), same(ref), isNull());
    IndexQueuePriority priority = IndexQueuePriority.HIGH;
    replayDefault();
    service.queue(ref, priority);
    verifyDefault();
    assertNotNull(eventCapture.getValue());
    assertEquals(priority, eventCapture.getValue().getPriority().get());
  }

  @Test
  public void test_queueDelete() {
    Capture<LuceneQueueDeleteEvent> eventCapture = EasyMock.newCapture();
    EntityReference ref = new DocumentReference("wiki", "space", "doc");
    getMock(ObservationManager.class).notify(capture(eventCapture), same(ref), isNull());
    replayDefault();
    service.queueDelete(ref);
    verifyDefault();
    assertNotNull(eventCapture.getValue());
    assertFalse(eventCapture.getValue().getPriority().isPresent());
  }

  @Test
  public void test_queueDelete_prio() {
    Capture<LuceneQueueDeleteEvent> eventCapture = EasyMock.newCapture();
    EntityReference ref = new DocumentReference("wiki", "space", "doc");
    getMock(ObservationManager.class).notify(capture(eventCapture), same(ref), isNull());
    IndexQueuePriority priority = IndexQueuePriority.HIGH;
    replayDefault();
    service.queueDelete(ref, priority);
    verifyDefault();
    assertNotNull(eventCapture.getValue());
    assertEquals(priority, eventCapture.getValue().getPriority().get());
  }

}
