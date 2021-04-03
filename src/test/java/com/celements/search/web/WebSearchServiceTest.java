package com.celements.search.web;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.search.web.classes.WebAttachmentSearchConfigClass;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.packages.ContentWebSearchPackage;
import com.celements.search.web.packages.MenuWebSearchPackage;
import com.celements.search.web.packages.WebSearchPackage;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class WebSearchServiceTest extends AbstractComponentTest {

  private DocumentReference docRef;

  private IWebSearchService searchService;

  private IModelAccessFacade modelAccessMock;

  @Before
  public void prepareTest() throws Exception {
    docRef = new DocumentReference("wiki", "space", "doc");
    getContext().setDatabase(docRef.getWikiReference().getName());
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    searchService = Utils.getComponent(IWebSearchService.class);
  }

  @Test
  public void test_getAvailablePackages_default() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(docRef);
    obj.setXClassReference(WebSearchConfigClass.CLASS_REF);
    doc.addXObject(obj);

    replayDefault();
    Collection<WebSearchPackage> ret = searchService.getAvailablePackages(doc);
    verifyDefault();

    assertEquals(2, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class, MenuWebSearchPackage.NAME)));
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

  @Test
  public void test_getAvailablePackages_configured() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(docRef);
    obj.setXClassReference(WebSearchConfigClass.CLASS_REF);
    obj.setStringValue(WebSearchConfigClass.FIELD_PACKAGES.getName(), ContentWebSearchPackage.NAME);
    doc.addXObject(obj);

    replayDefault();
    Collection<WebSearchPackage> ret = searchService.getAvailablePackages(doc);
    verifyDefault();

    assertEquals(1, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

  @Test
  public void test_getAvailablePackages_required() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(docRef);
    obj.setXClassReference(WebSearchConfigClass.CLASS_REF);
    doc.addXObject(obj);
    obj = new BaseObject();
    obj.setDocumentReference(docRef);
    obj.setXClassReference(WebAttachmentSearchConfigClass.CLASS_REF);
    doc.addXObject(obj);

    replayDefault();
    Collection<WebSearchPackage> ret = searchService.getAvailablePackages(doc);
    verifyDefault();

    assertEquals(3, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class, MenuWebSearchPackage.NAME)));
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

}
