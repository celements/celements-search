package com.celements.search.lucene;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.ObservationManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.observation.LuceneQueueEvent;
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
    EntityReference ref = new DocumentReference("wiki", "space", "doc");
    getMock(ObservationManager.class).notify(isA(LuceneQueueEvent.class), same(ref), isNull());
    replayDefault();
    service.queue(ref);
    verifyDefault();
  }

}
