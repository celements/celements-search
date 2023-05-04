package com.celements.search.lucene.query;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class LuceneQueryTest extends AbstractComponentTest {

  private LuceneQuery query;

  @Before
  public void prepareTest() throws Exception {
    query = new LuceneQuery();
    query.setWiki(new WikiReference("xwikidb"));
  }

  @Test
  public void testGetType() {
    assertEquals(Type.AND, query.getType());
  }

  @Test
  public void testGetDocTypes() {
    assertEquals(Collections.emptyList(), query.getDocTypes());
    List<LuceneDocType> docTypes = new ArrayList<>();
    docTypes.add(LuceneDocType.DOC);
    query.setDocTypes(docTypes);
    assertEquals(1, query.getDocTypes().size());
    assertEquals(LuceneDocType.DOC, query.getDocTypes().get(0));
    docTypes.add(LuceneDocType.ATT);
    assertEquals(1, query.getDocTypes().size());
    try {
      query.getDocTypes().remove(0);
      fail("expecting UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
  }

  @Test
  public void testGetWikis_defaultWiki() {
    assertEquals("xwikidb", query.getWikis().get(0).getName());
    try {
      query.getWikis().remove(0);
      fail("expecting UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
  }

  @Test
  public void testGetWikis_set() {
    List<WikiReference> wikis = new ArrayList<>();
    wikis.add(new WikiReference("wikiX"));
    query.setWikis(wikis);
    assertEquals(wikis, query.getWikis());
    wikis.add(new WikiReference("wikiY"));
    assertFalse(wikis.equals(query.getWikis()));
    try {
      query.getWikis().remove(0);
      fail("expecting UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
  }

  @Test
  public void testGetQueryString() {
    String queryString = "wiki:(+\"xwikidb\")";
    assertEquals("wiki restriction always needed", queryString, query.getQueryString());
    assertEquals("queryString must stay the same", queryString, query.getQueryString());
  }

  @Test
  public void testGetQueryString_withType() {
    query.setDocTypes(Arrays.asList(LuceneDocType.DOC));
    String queryString = "(type:(+\"" + LuceneDocType.DOC.key + "\") AND wiki:(+\"xwikidb\"))";
    assertEquals(queryString, query.getQueryString());
    assertEquals("queryString must stay the same", queryString, query.getQueryString());
  }

  @Test
  public void testGetQueryString_multiTypes() {
    query.setDocTypes(Arrays.asList(LuceneDocType.DOC, LuceneDocType.ATT));
    String queryString = "((type:(+\"" + LuceneDocType.DOC.key + "\") OR type:(+\""
        + LuceneDocType.ATT + "\")) AND wiki:(+\"xwikidb\"))";
    assertEquals(queryString, query.getQueryString());
    assertEquals("queryString must stay the same", queryString, query.getQueryString());
  }

  @Test
  public void testGetQueryString_otherDB() {
    query.setWiki(new WikiReference("otherwiki"));
    String queryString = "wiki:(+\"otherwiki\")";
    assertEquals(queryString, query.getQueryString());
    assertEquals("queryString must stay the same", queryString, query.getQueryString());
  }

  @Test
  public void testGetQueryString_multiDBs() {
    query.setWikis(Arrays.asList(new WikiReference("wiki1"), new WikiReference("wiki2")));
    String queryString = "(wiki:(+\"wiki1\") OR wiki:(+\"wiki2\"))";
    assertEquals(queryString, query.getQueryString());
    assertEquals("queryString must stay the same", queryString, query.getQueryString());
  }

  @Test
  public void testGetQueryString_emptyRestriction() {
    query.add(new QueryRestriction("object", ""));
    String queryString = "wiki:(+\"xwikidb\")";
    assertEquals(queryString, query.getQueryString());
    assertEquals("queryString must stay the same", queryString, query.getQueryString());
  }

  @Test
  public void testGetQueryString_oneRestr() {
    query.add(new QueryRestriction("object", "XWiki.XWikiUsers"));
    String queryString = "(wiki:(+\"xwikidb\") AND object:(+XWiki.XWikiUsers*))";
    assertEquals(queryString, query.getQueryString());
    assertEquals("queryString must stay the same", queryString, query.getQueryString());
  }

  @Test
  public void testGetQueryString_filled() {
    LuceneQuery query = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    String queryString = "(type:(+\"" + LuceneDocType.DOC + "\") "
        + "AND (field1:(+value1*) OR field2:(+value2*)) "
        + "AND (field3:(+value3*) OR field4:(+value4*)) AND field5:(+value5*))";
    assertEquals(queryString, query.getQueryString());
    assertEquals("queryString must stay the same", queryString, query.getQueryString());
  }

  @Test
  public void testCopy() {
    LuceneQuery query = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    LuceneQuery queryCopy = query.copy();
    assertNotSame(query, queryCopy);
    assertEquals(query, queryCopy);
    assertEquals(query.getDocTypes(), query.getDocTypes());
    assertEquals(query.getWikis(), query.getWikis());
    assertEquals(query.getQueryString(), query.getQueryString());
  }

  @Test
  public void testEquals() {
    LuceneQuery query = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    LuceneQuery queryCopy = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    assertNotSame(query, queryCopy);
    assertTrue(query.equals(queryCopy));

    queryCopy = getNewFilledQuery(Arrays.asList(LuceneDocType.ATT));
    assertFalse(query.equals(queryCopy));

    queryCopy = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    queryCopy.setWiki(new WikiReference("asdf"));
    assertFalse(query.equals(queryCopy));

    queryCopy = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    queryCopy.add(new QueryRestriction("field6", "value6"));
    assertFalse(query.equals(queryCopy));
  }

  @Test
  public void testHashCode() {
    LuceneQuery query = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    LuceneQuery queryCopy = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    assertNotSame(query, queryCopy);
    assertTrue(query.hashCode() == queryCopy.hashCode());

    queryCopy = getNewFilledQuery(Arrays.asList(LuceneDocType.ATT));
    assertFalse(query.hashCode() == queryCopy.hashCode());

    queryCopy = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    queryCopy.setWiki(new WikiReference("asdf"));
    assertFalse(query.hashCode() == queryCopy.hashCode());

    queryCopy = getNewFilledQuery(Arrays.asList(LuceneDocType.DOC));
    queryCopy.add(new QueryRestriction("field6", "value6"));
    assertFalse(query.hashCode() == queryCopy.hashCode());
  }

  private LuceneQuery getNewFilledQuery(List<LuceneDocType> docTypes) {
    LuceneQuery query = new LuceneQuery();
    query.setDocTypes(docTypes);
    QueryRestrictionGroup restrGrpUser = new QueryRestrictionGroup(Type.OR);
    restrGrpUser.add(new QueryRestriction("field1", "value1"));
    restrGrpUser.add(new QueryRestriction("field2", "value2"));
    query.add(restrGrpUser);
    QueryRestrictionGroup restrGrpGroup = new QueryRestrictionGroup(Type.OR);
    restrGrpGroup.add(new QueryRestriction("field3", "value3"));
    restrGrpGroup.add(new QueryRestriction("field4", "value4"));
    query.add(restrGrpGroup);
    query.add(new QueryRestriction("field5", "value5"));
    return query;
  }

}
