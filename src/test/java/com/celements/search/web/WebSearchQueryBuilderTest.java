package com.celements.search.web;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.text.MessageFormat;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.packages.AttachmentWebSearchPackage;
import com.celements.search.web.packages.ContentWebSearchPackage;
import com.celements.search.web.packages.MenuWebSearchPackage;
import com.celements.search.web.packages.WebSearchPackage;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class WebSearchQueryBuilderTest extends AbstractComponentTest {

  private static final String QUERY_START = "wiki:(+\"wiki\") AND NOT name:(+\"WebPreferences\") AND ";
  private static final String QUERY_CONTENT = "(type:(+\"wikipage\") AND ft:(+{0}*)^20)";
  private static final String QUERY_MENU = "(type:(+\"wikipage\") AND "
      + "(Celements2.MenuName.menu_name:(+{0}*)^30 OR title:(+{0}*)^30))";
  private static final String QUERY_ATTACHMENT = "(type:(+\"attachment\") AND ft:(+{0}*)^20)";

  private DocumentReference docRef;
  private WebSearchQueryBuilder builder;

  @Before
  public void prepareTest() throws Exception {
    docRef = new DocumentReference("wiki", "space", "doc");
    getContext().setDatabase(docRef.getWikiReference().getName());
    builder = Utils.getComponent(WebSearchQueryBuilder.class);
  }

  @Test
  public void test_loadIWebSearchService() {
    IWebSearchService searchService = Utils.getComponent(IWebSearchService.class);
    System.out.println(
        "<<<<<<<<<<<<<<< WebSearchQueryBuilderTest test_loadIWebSearchService searchService: "
            + searchService);
  }

  @Test
  public void test_getPackages_default() {
    builder.setConfigDoc(createCfDoc(docRef, false));

    replayDefault();
    Collection<WebSearchPackage> ret = builder.getPackages();
    verifyDefault();

    assertEquals(2, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class, MenuWebSearchPackage.NAME)));
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

  @Test
  public void test_addPackage() {
    builder.setConfigDoc(createCfDoc(docRef, false));
    WebSearchPackage webPackage = Utils.getComponent(WebSearchPackage.class,
        AttachmentWebSearchPackage.NAME);
    builder.addPackage(webPackage);

    replayDefault();
    Collection<WebSearchPackage> ret = builder.getPackages();
    verifyDefault();

    assertEquals(1, ret.size());
    assertTrue(ret.contains(webPackage));
  }

  @Test
  public void test_build_noTerm() throws Exception {
    builder.setConfigDoc(createCfDoc(docRef, false));

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(buildQueryString("type:(+\"wikipage\")"), query.getQueryString());
  }

  @Test
  public void test_build_content() throws Exception {
    String searchTerm = "welt";
    builder.setConfigDoc(createCfDoc(docRef, false));
    builder.setSearchTerm(searchTerm);
    builder.addPackage(ContentWebSearchPackage.NAME);

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(builder.getPackages().size(), 1);
    assertEquals(buildQueryString(QUERY_CONTENT, searchTerm), query.getQueryString());
  }

  @Test
  public void test_build_menu() throws Exception {
    String searchTerm = "welt";
    builder.setConfigDoc(createCfDoc(docRef, false));
    builder.setSearchTerm(searchTerm);
    builder.addPackage(MenuWebSearchPackage.NAME);

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(builder.getPackages().size(), 1);
    assertEquals(buildQueryString(QUERY_MENU, searchTerm), query.getQueryString());
  }

  @Test
  public void test_build_attachment() throws Exception {
    String searchTerm = "welt";
    builder.setConfigDoc(createCfDoc(docRef, false));
    builder.setSearchTerm(searchTerm);
    builder.addPackage(AttachmentWebSearchPackage.NAME);

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(builder.getPackages().size(), 1);
    assertEquals(buildQueryString(QUERY_ATTACHMENT, searchTerm), query.getQueryString());
  }

  @Test
  public void test_build_linkedDocsOnly() {
    String searchTerm = "welt";
    builder.setConfigDoc(createCfDoc(docRef, true));
    builder.setSearchTerm(searchTerm);
    builder.addPackage(MenuWebSearchPackage.NAME);
    builder.addPackage(ContentWebSearchPackage.NAME);
    builder.addPackage(AttachmentWebSearchPackage.NAME);

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

  private XWikiDocument createCfDoc(DocumentReference docRef, boolean linkedDocsOnly) {
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
