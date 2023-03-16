package com.celements.search.lucene.query;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class QueryRestrictionTest {

  @Before
  public void setUp_QueryRestrictionTest() throws Exception {}

  @Test
  public void testGetQueryString_nothingSet() {
    QueryRestriction restr = new QueryRestriction(null, null);
    assertEquals("", restr.getQueryString());
  }

  @Test
  public void testGetQueryString_specifierNotSet() {
    QueryRestriction restr = new QueryRestriction(null, "Hans Peter");
    restr.setQuery("Hans Peter");
    assertEquals("", restr.getQueryString());
  }

  @Test
  public void testGetQueryString_queryNotSet() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", null);
    assertEquals("", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString_quoted() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name",
        "Hans \"Robert Peter\"");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +\"Robert Peter\")", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString_singleMinus() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans 50% - 100%");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +50%* +100%*)", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString_sinleMinusQuoted() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name",
        "Hans \"50% - 100%\"");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +\"50% \\- 100%\")", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString_plus() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "+Hans");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans*)", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString_minus() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans -Peter");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* -Peter*)", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString_wildcardOne() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Pe?er");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Pe?er*)", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString_wildcardMultipleMiddle() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans P*er");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +P*er*)", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringString_wildcardMultipleEnd() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Pet*");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Pet*)", restr.getQueryString());
  }

  @Test
  public void testQueryRestrictionStringStringBoolean() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter",
        false);
    assertEquals("XWiki.XWikiUsers.first_name:(Hans Peter)", restr.getQueryString());
  }

  @Test
  public void testSetSpecifier() {
    QueryRestriction restr = new QueryRestriction("xyz", "XWiki.XWikiUsers");
    restr.setSpecifier("object");
    assertEquals("object:(+XWiki.XWikiUsers*)", restr.getQueryString());
  }

  @Test
  public void testSetQuery() {
    QueryRestriction restr = new QueryRestriction("object", "xyz");
    restr.setQuery("XWiki.XWikiUsers");
    assertEquals("object:(+XWiki.XWikiUsers*)", restr.getQueryString());
  }

  @Test
  public void testSetTokenizeQuery_true() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setTokenizeQuery(true);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", restr.getQueryString());
  }

  @Test
  public void testSetTokenizeQuery_false() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:(Hans Peter)", restr.getQueryString());
  }

  @Test
  public void testRangeQueryInclusive() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "[Hans TO Peter]");
    restr.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:([Hans TO Peter])", restr.getQueryString());
  }

  @Test
  public void testRangeQueryExclusive() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "{Hans TO Peter}");
    restr.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:({Hans TO Peter})", restr.getQueryString());
  }

  @Test
  public void testSetFuzzy_toHigh() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setFuzzy(1.3f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~) AND (Peter* OR Peter~))",
        restr.getQueryString());
  }

  @Test
  public void testSetFuzzy_toLow() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setFuzzy(-0.2f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~) AND (Peter* OR Peter~))",
        restr.getQueryString());
  }

  @Test
  public void testSetFuzzy_valid() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setFuzzy(.8f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~0.8) " + "AND (Peter* OR Peter~0.8))",
        restr.getQueryString());
  }

  @Test
  public void testSetFuzzy_valid_long() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setFuzzy(.888888888888888f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~0.889) "
        + "AND (Peter* OR Peter~0.889))", restr.getQueryString());
  }

  @Test
  public void testSetProximity_negative() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setProximity(-8);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", restr.getQueryString());
  }

  @Test
  public void testSetProximity_valid() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setProximity(8);
    assertEquals("XWiki.XWikiUsers.first_name:(\"+Hans +Peter\"~8)", restr.getQueryString());
  }

  @Test
  public void testSetBoost_negative() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setBoost(-8f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", restr.getQueryString());
  }

  @Test
  public void testSetBoost_small() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setBoost(.83333333333333f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^0.833", restr.getQueryString());
  }

  @Test
  public void testSetBoost() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setBoost(8f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^8", restr.getQueryString());
  }

  @Test
  public void testSetBoost_crazyHigh() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    restr.setBoost(8000.01f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^8000.01", restr.getQueryString());
  }

  @Test
  public void test_QueryRestrictionStringString() {
    QueryRestriction restr = new QueryRestriction("XWiki.XWikiUsers.first_name", "Hans Peter");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", restr.getQueryString());
  }
}
