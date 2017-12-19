package com.celements.search.web;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.search.web.classes.WebAttachmentSearchConfigClass;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.packages.ContentWebSearchPackage;
import com.celements.search.web.packages.MenuWebSearchPackage;
import com.celements.search.web.packages.WebSearchPackage;
import com.google.common.base.Optional;
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
    ClassReference attClassRef = Utils.getComponent(ClassDefinition.class,
        WebAttachmentSearchConfigClass.CLASS_DEF_HINT).getClassReference();
    ClassReference webSearchClassRef = Utils.getComponent(ClassDefinition.class,
        WebSearchConfigClass.CLASS_DEF_HINT).getClassReference();
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(doc).atLeastOnce();
    List<WebSearchPackage> emptyList = Collections.emptyList();
    expect(modelAccessMock.getFieldValue(same(doc), eq(
        WebSearchConfigClass.FIELD_PACKAGES))).andReturn(Optional.of(emptyList)).once();
    BaseObject obj = new BaseObject();
    obj.setXClassReference(webSearchClassRef.getDocRef(docRef.getWikiReference()));
    doc.addXObject(obj);
    expect(modelAccessMock.getXObject(doc, webSearchClassRef.getDocRef())).andReturn(
        obj).anyTimes();
    expect(modelAccessMock.getXObject(doc, attClassRef.getDocRef())).andReturn(null).anyTimes();

    replayDefault();
    Collection<WebSearchPackage> ret = searchService.getAvailablePackages(
        doc.getDocumentReference());
    verifyDefault();

    assertEquals(2, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class, MenuWebSearchPackage.NAME)));
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

  @Test
  public void test_getAvailablePackages_configured() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    ClassReference attClassRef = Utils.getComponent(ClassDefinition.class,
        WebAttachmentSearchConfigClass.CLASS_DEF_HINT).getClassReference();
    ClassReference webSearchClassRef = Utils.getComponent(ClassDefinition.class,
        WebSearchConfigClass.CLASS_DEF_HINT).getClassReference();
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(doc).atLeastOnce();
    List<WebSearchPackage> webSearchPackages = Arrays.asList(Utils.getComponent(
        WebSearchPackage.class, ContentWebSearchPackage.NAME));
    expect(modelAccessMock.getFieldValue(same(doc), eq(
        WebSearchConfigClass.FIELD_PACKAGES))).andReturn(Optional.of(webSearchPackages)).once();
    BaseObject obj = new BaseObject();
    obj.setXClassReference(webSearchClassRef.getDocRef(docRef.getWikiReference()));
    doc.addXObject(obj);
    expect(modelAccessMock.getXObject(doc, webSearchClassRef.getDocRef())).andReturn(
        obj).anyTimes();
    expect(modelAccessMock.getXObject(doc, attClassRef.getDocRef())).andReturn(null).anyTimes();

    replayDefault();
    Collection<WebSearchPackage> ret = searchService.getAvailablePackages(
        doc.getDocumentReference());
    verifyDefault();

    assertEquals(1, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

  @Test
  public void test_getAvailablePackages_required() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    ClassReference attClassRef = Utils.getComponent(ClassDefinition.class,
        WebAttachmentSearchConfigClass.CLASS_DEF_HINT).getClassReference();
    ClassReference webSearchClassRef = Utils.getComponent(ClassDefinition.class,
        WebSearchConfigClass.CLASS_DEF_HINT).getClassReference();
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(doc).atLeastOnce();
    List<WebSearchPackage> emptyList = Collections.emptyList();
    expect(modelAccessMock.getFieldValue(same(doc), eq(
        WebSearchConfigClass.FIELD_PACKAGES))).andReturn(Optional.of(emptyList)).once();
    BaseObject obj = new BaseObject();
    obj.setXClassReference(webSearchClassRef.getDocRef(docRef.getWikiReference()));
    doc.addXObject(obj);
    expect(modelAccessMock.getXObject(doc, webSearchClassRef.getDocRef())).andReturn(
        obj).anyTimes();
    obj = new BaseObject();
    obj.setXClassReference(attClassRef.getDocRef(docRef.getWikiReference()));
    doc.addXObject(obj);
    expect(modelAccessMock.getXObject(doc, attClassRef.getDocRef())).andReturn(obj).anyTimes();

    replayDefault();
    Collection<WebSearchPackage> ret = searchService.getAvailablePackages(
        doc.getDocumentReference());
    verifyDefault();

    assertEquals(3, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class, MenuWebSearchPackage.NAME)));
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

}
