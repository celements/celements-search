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
import org.xwiki.observation.ObservationManager;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteLocalEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexLocalEvent;
import com.xpn.xwiki.web.Utils;

public class QueueWikiEventConverterTest extends AbstractComponentTest {

  QueueWikiEventConverter listener;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(ObservationManager.class);
    listener = (QueueWikiEventConverter) Utils.getComponent(EventListener.class,
        QueueWikiEventConverter.NAME);
  }

  @Test
  public void test_remote() {
    assertTrue("needs to listen on remote events", listener instanceof AbstractRemoteEventListener);
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
    getMock(ObservationManager.class).notify(isA(LuceneQueueIndexLocalEvent.class),
        eq(wikiRef), isNull());

    replayDefault();
    listener.onEvent(new WikiCreatedEvent(wikiRef.getName()), wikiRef, null);
    verifyDefault();
  }

  @Test
  public void test_onEvent_deleted() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    getMock(ObservationManager.class).notify(isA(LuceneQueueDeleteLocalEvent.class),
        eq(wikiRef), isNull());

    replayDefault();
    listener.onEvent(new WikiDeletedEvent(wikiRef.getName()), wikiRef, null);
    verifyDefault();
  }

}
