package com.celements.search.web.classes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.common.classes.AbstractClassCollection;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("WebSearchClasses")
public class WebSearchClasses extends AbstractClassCollection {

  private static Log LOGGER = LogFactory.getFactory().getInstance(WebSearchClasses.class);

  @Requirement
  private IWebSearchClassConfig classConf;

  @Requirement
  private IWebUtilsService webUtils;

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  public String getConfigName() {
    return "webSearch";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getWebSearchConfigClass();
    getWebAttachmentSearchConfigClass();
  }

  private BaseClass getWebSearchConfigClass() throws XWikiException {
    XWikiDocument classDoc = getClassDoc(classConf.getWebSearchConfigClassRef(
        webUtils.getWikiRef()));
    BaseClass bclass = classDoc.getXClass();
    boolean needsUpdate = classDoc.isNew();
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_PACKAGES,
        IWebSearchClassConfig.PROPERTY_PACKAGES, 30);
    needsUpdate |= bclass.addBooleanField(IWebSearchClassConfig.PROPERTY_LINKED_DOCS_ONLY,
        IWebSearchClassConfig.PROPERTY_LINKED_DOCS_ONLY, "yesno");
    needsUpdate |= bclass.addNumberField(IWebSearchClassConfig.PROPERTY_FUZZY_SEARCH, 
        IWebSearchClassConfig.PROPERTY_FUZZY_SEARCH, 15, "float");
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_DOCS,
        IWebSearchClassConfig.PROPERTY_DOCS, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_DOCS_BLACK_LIST,
        IWebSearchClassConfig.PROPERTY_DOCS_BLACK_LIST, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_SPACES,
        IWebSearchClassConfig.PROPERTY_SPACES, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_SPACES_BLACK_LIST,
        IWebSearchClassConfig.PROPERTY_SPACES_BLACK_LIST, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_PAGETYPES,
        IWebSearchClassConfig.PROPERTY_PAGETYPES, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_PAGETYPES_BLACK_LIST,
        IWebSearchClassConfig.PROPERTY_PAGETYPES_BLACK_LIST, 30);
    needsUpdate |= bclass.addBooleanField(IWebSearchClassConfig.PROPERTY_HIDE_FORM,
        IWebSearchClassConfig.PROPERTY_HIDE_FORM, "yesno");
    setContentAndSaveClassDocument(classDoc, needsUpdate);
    return bclass;
  }

  private BaseClass getWebAttachmentSearchConfigClass() throws XWikiException {
    XWikiDocument classDoc = getClassDoc(classConf.getWebAttachmentSearchConfigClassRef(
        webUtils.getWikiRef()));
    BaseClass bclass = classDoc.getXClass();
    boolean needsUpdate = classDoc.isNew();
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_MIMETYPE,
        IWebSearchClassConfig.PROPERTY_MIMETYPE, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_FILENAME_PREFIXES,
        IWebSearchClassConfig.PROPERTY_FILENAME_PREFIXES, 30);
    setContentAndSaveClassDocument(classDoc, needsUpdate);
    return bclass;
  }

}