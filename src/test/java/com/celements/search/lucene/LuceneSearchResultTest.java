package com.celements.search.lucene;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.query.LuceneQuery;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.plugin.lucene.SearchResult;
import com.xpn.xwiki.plugin.lucene.SearchResults;

public class LuceneSearchResultTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private LucenePlugin lucenePluginMock;

  private LuceneSearchResult result;

  @Before
  public void setUp_LuceneSearchResultTest() throws Exception {
    context = getContext();
    lucenePluginMock = createMockAndAddToDefault(LucenePlugin.class);
    newResult(new LuceneQuery(Arrays.asList("wikipage")), null, null, false);
    result.injectLucenePlugin(lucenePluginMock);
  }

  @Test
  public void testGetters() {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    List<String> sortFields = Arrays.asList("sort1", "sort2");
    List<String> languages = Arrays.asList("lang1", "lang2");
    boolean skipChecks = false;
    newResult(query, sortFields, languages, skipChecks);
    assertEquals(query.getQueryString(), result.getQueryString());
    assertEquals(sortFields, result.getSortFields());
    assertEquals(languages, result.getLanguages());
    assertEquals(skipChecks, result.isSkipChecks());
  }

  @Test
  public void testGetSetOffset() {
    assertEquals(0, result.getOffset());
    result.setOffset(6);
    assertEquals(6, result.getOffset());
  }

  @Test
  public void testGetSetLimit() {
    assertEquals(0, result.getLimit());
    result.setLimit(6);
    assertEquals(6, result.getLimit());
  }

  @Test
  public void testGetResults() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = true;
    newResult(query, null, null, skipChecks);
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);
    result.injectSearchResultsCache(sResultsMock);
    result.setOffset(6);
    result.setLimit(10);

    List<SearchResult> list = new ArrayList<>();
    list.add(createMockAndAddToDefault(SearchResult.class));
    list.add(createMockAndAddToDefault(SearchResult.class));
    expect(sResultsMock.getResults(eq(7), eq(10))).andReturn(list).once();
    DocumentReference docRef = new DocumentReference("db", "space", "doc");
    expect(list.get(0).getReference()).andReturn(docRef).once();
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    expect(list.get(1).getReference()).andReturn(attRef).once();

    replayDefault();
    List<EntityReference> ret = result.getResults();
    verifyDefault();

    assertEquals(Arrays.asList(docRef, attRef), ret);
  }

  @Test
  public void testGetResults_empty() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = true;
    newResult(query, null, null, skipChecks);
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);
    result.injectSearchResultsCache(sResultsMock);
    int totalCount = 10;

    expect(sResultsMock.getTotalHitcount()).andReturn(totalCount).once();
    expect(sResultsMock.getResults(eq(1), eq(totalCount))).andReturn(
        Collections.<SearchResult>emptyList()).once();

    replayDefault();
    List<EntityReference> ret = result.getResults();
    verifyDefault();

    assertNotNull(ret);
    assertEquals(0, ret.size());
  }

  @Test
  public void testGetResults_negativeOffsetLimit() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = false;
    newResult(query, null, null, skipChecks);
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);
    result.injectSearchResultsCache(sResultsMock);
    result.setOffset(-10);
    result.setLimit(-10);

    expect(sResultsMock.getHitcount()).andReturn(1234);
    List<SearchResult> list = new ArrayList<>();
    list.add(createMockAndAddToDefault(SearchResult.class));
    expect(sResultsMock.getResults(eq(1), eq(1234))).andReturn(list).once();
    DocumentReference docRef = new DocumentReference("db", "space", "doc");
    expect(list.get(0).getReference()).andReturn(docRef).once();

    replayDefault();
    List<EntityReference> ret = result.getResults();
    verifyDefault();

    assertEquals(Arrays.asList(docRef), ret);
  }

  @Test
  public void testGetResultsScoreMap() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = true;
    newResult(query, null, null, skipChecks);
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);
    result.injectSearchResultsCache(sResultsMock);
    result.setOffset(6);
    result.setLimit(10);

    List<SearchResult> list = new ArrayList<>();
    list.add(createMockAndAddToDefault(SearchResult.class));
    list.add(createMockAndAddToDefault(SearchResult.class));
    expect(sResultsMock.getResults(eq(7), eq(10))).andReturn(list).once();
    DocumentReference docRef = new DocumentReference("db", "space", "doc");
    expect(list.get(0).getReference()).andReturn(docRef).once();
    Float docScore = 0.3f;
    expect(list.get(0).getScore()).andReturn(docScore).once();
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    expect(list.get(1).getReference()).andReturn(attRef).once();
    Float attScore = 0.5f;
    expect(list.get(1).getScore()).andReturn(attScore).once();

    replayDefault();
    Map<EntityReference, Float> ret = result.getResultsScoreMap();
    verifyDefault();

    assertEquals(2, ret.size());
    assertTrue(ret.containsKey(docRef));
    assertEquals(docScore, ret.get(docRef));
    assertTrue(ret.containsKey(attRef));
    assertEquals(attScore, ret.get(attRef));
  }

  @Test
  public void testGetResultsScoreMap_empty() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = true;
    newResult(query, null, null, skipChecks);
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);
    result.injectSearchResultsCache(sResultsMock);
    int totalCount = 10;

    expect(sResultsMock.getTotalHitcount()).andReturn(totalCount).once();
    expect(sResultsMock.getResults(eq(1), eq(totalCount))).andReturn(
        Collections.<SearchResult>emptyList()).once();

    replayDefault();
    Map<EntityReference, Float> ret = result.getResultsScoreMap();
    verifyDefault();

    assertNotNull(ret);
    assertEquals(0, ret.size());
  }

  @Test
  public void testGetSize() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = false;
    newResult(query, null, null, skipChecks);
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);
    result.injectSearchResultsCache(sResultsMock);

    expect(sResultsMock.getHitcount()).andReturn(1234);

    replayDefault();
    int ret = result.getSize();
    verifyDefault();

    assertEquals(1234, ret);
  }

  @Test
  public void testGetSize_skipChecks() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = true;
    newResult(query, null, null, skipChecks);
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);
    result.injectSearchResultsCache(sResultsMock);

    expect(sResultsMock.getTotalHitcount()).andReturn(1234);

    replayDefault();
    int ret = result.getSize();
    verifyDefault();

    assertEquals(1234, ret);
  }

  @Test
  public void testLuceneSearch_alreadySet() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = true;
    newResult(query, null, null, skipChecks);
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);
    result.injectSearchResultsCache(sResultsMock);

    replayDefault();
    SearchResults ret = result.luceneSearch();
    verifyDefault();

    assertSame(sResultsMock, ret);
  }

  @Test
  public void testLuceneSearch_withChecks() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    List<String> sortFields = Arrays.asList("sort1", "sort2");
    List<String> languages = Arrays.asList("lang1", "lang2");
    boolean skipChecks = false;
    newResult(query, sortFields, languages, skipChecks);
    Capture<String[]> sortFieldsCapture = newCapture();
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);

    expect(lucenePluginMock.getSearchResults(eq(query.getQueryString()), capture(sortFieldsCapture),
        isNull(String.class), eq("lang1,lang2"), same(context))).andReturn(sResultsMock).once();

    replayDefault();
    SearchResults ret = result.luceneSearch();
    verifyDefault();

    assertEquals(sortFields, Arrays.asList(sortFieldsCapture.getValue()));
    assertSame(sResultsMock, ret);
  }

  @Test
  public void testLuceneSearch_withoutChecks() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = true;
    newResult(query, null, null, skipChecks);
    Capture<String[]> sortFieldsCapture = newCapture();
    SearchResults sResultsMock = createMockAndAddToDefault(SearchResults.class);

    expect(lucenePluginMock.getSearchResultsWithoutChecks(eq(query.getQueryString()), capture(
        sortFieldsCapture), isNull(String.class), eq(""), same(context))).andReturn(
            sResultsMock).once();

    replayDefault();
    SearchResults ret = result.luceneSearch();
    verifyDefault();

    assertEquals(Collections.emptyList(), Arrays.asList(sortFieldsCapture.getValue()));
    assertSame(sResultsMock, ret);
  }

  @Test
  public void testLuceneSearch_IOException() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = false;
    newResult(query, null, null, skipChecks);

    expect(lucenePluginMock.getSearchResults(eq(query.getQueryString()), anyObject(String[].class),
        isNull(String.class), eq(""), same(context))).andThrow(new IOException()).once();

    replayDefault();
    try {
      result.luceneSearch();
      fail("expected LuceneSearchException");
    } catch (LuceneSearchException lse) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testLuceneSearch_ParseException() throws Exception {
    LuceneQuery query = new LuceneQuery(Arrays.asList("wikipage"));
    boolean skipChecks = false;
    newResult(query, null, null, skipChecks);

    expect(lucenePluginMock.getSearchResults(eq(query.getQueryString()), anyObject(String[].class),
        isNull(String.class), eq(""), same(context))).andThrow(new ParseException()).once();

    replayDefault();
    try {
      result.luceneSearch();
      fail("expected LuceneSearchException");
    } catch (LuceneSearchException lse) {
      // expected
    }
    verifyDefault();
  }

  private LuceneSearchResult newResult(LuceneQuery query, List<String> sortFields,
      List<String> languages, boolean skipChecks) {
    result = new LuceneSearchResult(query, sortFields, languages, skipChecks);
    result.injectLucenePlugin(lucenePluginMock);
    return result;
  }

}
