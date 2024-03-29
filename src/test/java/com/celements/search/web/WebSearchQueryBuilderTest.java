package com.celements.search.web;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.classes.WebSearchFieldConfigClass;
import com.celements.search.web.packages.AttachmentWebSearchPackage;
import com.celements.search.web.packages.ContentWebSearchPackage;
import com.celements.search.web.packages.FieldWebSearchPackage;
import com.celements.search.web.packages.MenuWebSearchPackage;
import com.celements.search.web.packages.WebSearchPackage;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

public class WebSearchQueryBuilderTest extends AbstractComponentTest {

  private static final String QUERY_START = "wiki:(+\"wiki\") AND NOT name:(+\"WebPreferences\") AND ";
  private static final String QUERY_CONTENT = "(type:(+\"wikipage\") AND "
      + "(ft:(+{0}*)^20 OR ft:(\"{0}\")^40))";
  private static final String QUERY_MENU = "(type:(+\"wikipage\") AND "
      + "(Celements2.MenuName.menu_name:(+{0}*)^30 OR title:(+{0}*)^30))";
  private static final String QUERY_FIELD = "(type:(+\"wikipage\") AND "
      + "(somefield:(+{0}*)^1 OR somefield:(\"{0}\")^2))";
  private static final String QUERY_ATTACHMENT = "(type:(+\"attachment\") AND "
      + "(ft:(+{0}*)^20 OR ft:(\"{0}\")^40))";

  private DocumentReference docRef;
  private WebSearchQueryBuilder builder;
  private IWebSearchService webSearchServiceMock;

  @Before
  public void prepareTest() throws Exception {
    docRef = new DocumentReference("wiki", "space", "doc");
    getContext().setDatabase(docRef.getWikiReference().getName());
    LucenePlugin lucenePlugin = createMockAndAddToDefault(LucenePlugin.class);
    expect(getWikiMock().getPlugin(eq("lucene"), same(getContext())))
        .andReturn(lucenePlugin).anyTimes();
    expect(lucenePlugin.getAnalyzer()).andReturn(null).anyTimes();
    webSearchServiceMock = registerComponentMock(IWebSearchService.class);
    builder = Utils.getComponent(WebSearchQueryBuilder.class);
  }

  @Test
  public void test_getPackages_default() throws Exception {
    XWikiDocument cfgDoc = createCfgDoc(docRef, false);
    builder.setConfigDoc(cfgDoc);

    Set<WebSearchPackage> webSearchPackage = ImmutableSet.<WebSearchPackage>of(Utils.getComponent(
        WebSearchPackage.class, MenuWebSearchPackage.NAME),
        Utils.getComponent(WebSearchPackage.class, ContentWebSearchPackage.NAME));
    expect(webSearchServiceMock.getAvailablePackages(cfgDoc)).andReturn(webSearchPackage)
        .atLeastOnce();

    replayDefault();
    Collection<WebSearchPackage> ret = builder.getPackages();
    verifyDefault();

    assertEquals(2, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class, MenuWebSearchPackage.NAME)));
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

  @Test
  public void test_addPackage() throws Exception {
    XWikiDocument cfgDoc = createCfgDoc(docRef, false);
    builder.setConfigDoc(cfgDoc);
    WebSearchPackage webSearchPackage = Utils.getComponent(WebSearchPackage.class,
        AttachmentWebSearchPackage.NAME);
    builder.addPackage(webSearchPackage);
    expect(webSearchServiceMock.getAvailablePackages(cfgDoc))
        .andReturn(ImmutableSet.<WebSearchPackage>of(webSearchPackage))
        .atLeastOnce();

    replayDefault();
    Collection<WebSearchPackage> ret = builder.getPackages();
    verifyDefault();

    assertEquals(1, ret.size());
    assertTrue(ret.contains(webSearchPackage));
  }

  @Test
  public void test_build_noTerm() throws Exception {
    XWikiDocument cfgDoc = createCfgDoc(docRef, false);
    builder.setConfigDoc(cfgDoc);
    Set<WebSearchPackage> webSearchPackages = ImmutableSet.<WebSearchPackage>of(Utils.getComponent(
        WebSearchPackage.class, MenuWebSearchPackage.NAME),
        Utils.getComponent(
            WebSearchPackage.class, ContentWebSearchPackage.NAME));
    expect(webSearchServiceMock.getAvailablePackages(cfgDoc)).andReturn(webSearchPackages)
        .atLeastOnce();

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(buildQueryString("type:(+\"wikipage\")"), query.getQueryString());
  }

  @Test
  public void test_build_content() throws Exception {
    String searchTerm = "welt";
    XWikiDocument cfgDoc = createCfgDoc(docRef, false);
    builder.setConfigDoc(cfgDoc);
    builder.setSearchTerm(searchTerm);
    builder.addPackage(ContentWebSearchPackage.NAME);

    expect(webSearchServiceMock.getAvailablePackages(cfgDoc)).andReturn(
        ImmutableSet.<WebSearchPackage>of(
            Utils.getComponent(WebSearchPackage.class, ContentWebSearchPackage.NAME)))
        .atLeastOnce();

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(1, builder.getPackages().size());
    assertEquals(buildQueryString(QUERY_CONTENT, searchTerm), query.getQueryString());
  }

  @Test
  public void test_build_menu() throws Exception {
    String searchTerm = "welt";
    XWikiDocument cfgDoc = createCfgDoc(docRef, false);
    builder.setConfigDoc(cfgDoc);
    builder.setSearchTerm(searchTerm);
    builder.addPackage(MenuWebSearchPackage.NAME);

    expect(webSearchServiceMock.getAvailablePackages(cfgDoc)).andReturn(
        ImmutableSet.<WebSearchPackage>of(Utils.getComponent(WebSearchPackage.class,
            MenuWebSearchPackage.NAME)))
        .atLeastOnce();

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(1, builder.getPackages().size());
    assertEquals(buildQueryString(QUERY_MENU, searchTerm), query.getQueryString());
  }

  @Test
  public void test_build_field() throws Exception {
    String searchTerm = "welt";
    XWikiDocument cfgDoc = createCfgDoc(docRef, false);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(WebSearchFieldConfigClass.CLASS_REF);
    obj.setStringValue(WebSearchFieldConfigClass.FIELD_NAME.getName(), "somefield");
    cfgDoc.addXObject(obj);
    builder.setConfigDoc(cfgDoc);
    builder.setSearchTerm(searchTerm);
    builder.addPackage(FieldWebSearchPackage.NAME);

    expect(webSearchServiceMock.getAvailablePackages(cfgDoc)).andReturn(
        ImmutableSet.<WebSearchPackage>of(Utils.getComponent(WebSearchPackage.class,
            FieldWebSearchPackage.NAME)))
        .atLeastOnce();

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(1, builder.getPackages().size());
    assertEquals(buildQueryString(QUERY_FIELD, searchTerm), query.getQueryString());
  }

  @Test
  public void test_build_attachment() throws Exception {
    String searchTerm = "welt";
    XWikiDocument cfgDoc = createCfgDoc(docRef, false);
    builder.setConfigDoc(cfgDoc);
    builder.setSearchTerm(searchTerm);
    builder.addPackage(AttachmentWebSearchPackage.NAME);

    expect(webSearchServiceMock.getAvailablePackages(cfgDoc)).andReturn(
        ImmutableSet.<WebSearchPackage>of(Utils.getComponent(WebSearchPackage.class,
            AttachmentWebSearchPackage.NAME)))
        .atLeastOnce();

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(1, builder.getPackages().size());
    assertEquals(buildQueryString(QUERY_ATTACHMENT, searchTerm), query.getQueryString());
  }

  @Test
  public void test_build_linkedDocsOnly() throws Exception {
    String searchTerm = "welt";
    XWikiDocument cfgDoc = createCfgDoc(docRef, true);
    builder.setConfigDoc(cfgDoc);
    builder.setSearchTerm(searchTerm);
    builder.addPackage(MenuWebSearchPackage.NAME);
    builder.addPackage(ContentWebSearchPackage.NAME);
    builder.addPackage(AttachmentWebSearchPackage.NAME);

    expect(webSearchServiceMock.getAvailablePackages(cfgDoc)).andReturn(
        ImmutableSet.<WebSearchPackage>of(
            Utils.getComponent(WebSearchPackage.class, MenuWebSearchPackage.NAME),
            Utils.getComponent(WebSearchPackage.class, ContentWebSearchPackage.NAME),
            Utils.getComponent(WebSearchPackage.class, AttachmentWebSearchPackage.NAME)))
        .atLeastOnce();

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    String queryStr = "(" + QUERY_MENU + " OR " + QUERY_CONTENT + " OR " + QUERY_ATTACHMENT + ")"
        + " AND (object:(+Celements2.MenuItem*) OR type:(+\"attachment\"))";
    assertEquals(buildQueryString(queryStr, searchTerm), query.getQueryString());
  }

  private String buildQueryString(String query, Object... arguments) {
    return MessageFormat.format("(" + QUERY_START + query + ")", arguments);
  }

  private XWikiDocument createCfgDoc(DocumentReference docRef, boolean linkedDocsOnly) {
    ClassReference classRef = Utils.getComponent(ClassDefinition.class,
        WebSearchConfigClass.CLASS_DEF_HINT).getClassReference();
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj = new BaseObject();
    obj.setIntValue(WebSearchConfigClass.FIELD_LINKED_DOCS_ONLY.getName(), linkedDocsOnly ? 1 : 0);
    obj.setXClassReference(classRef.getDocRef(docRef.getWikiReference()));
    doc.addXObject(obj);
    return doc;
  }

}
