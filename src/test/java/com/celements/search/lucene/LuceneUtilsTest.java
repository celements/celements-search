package com.celements.search.lucene;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class LuceneUtilsTest extends AbstractComponentTest {

  @Test
  public void test_exactifyRestrictionString() throws Exception {
    replayDefault();
    String exactRestriction1 = LuceneUtils.exactify("test.restriction1");
    String exactRestriction2 = LuceneUtils.exactify("\"test.restriction2");
    String exactRestriction3 = LuceneUtils.exactify("test.restriction3\"");
    String exactRestriction4 = LuceneUtils.exactify("\"test.restriction4\"");
    verifyDefault();
    assertEquals("\"test.restriction1\"", exactRestriction1);
    assertEquals("\"test.restriction2\"", exactRestriction2);
    assertEquals("\"test.restriction3\"", exactRestriction3);
    assertEquals("\"test.restriction4\"", exactRestriction4);
  }

}
