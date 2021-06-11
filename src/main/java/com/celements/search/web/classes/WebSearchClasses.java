package com.celements.search.web.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.common.classes.AbstractClassCollection;
import com.celements.model.classes.ClassDefinition;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * @deprecated instead use {@link ClassDefinition}
 */
@Deprecated
@Component("WebSearchClasses")
public class WebSearchClasses extends AbstractClassCollection {

  @Requirement
  private IWebSearchClassConfig classConf;

  @Requirement
  private IWebUtilsService webUtils;

  @Override
  public String getConfigName() {
    return "webSearch";
  }

  @Override
  protected void initClasses() throws XWikiException {
    LOGGER.warn("class collection 'webSearch' is deprecated and should be disabled");
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
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_SORT_FIELDS,
        IWebSearchClassConfig.PROPERTY_SORT_FIELDS, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_RESULT_ITEM_RENDER_SCRIPT,
        IWebSearchClassConfig.PROPERTY_RESULT_ITEM_RENDER_SCRIPT, 30);
    setContentAndSaveClassDocument(classDoc, needsUpdate);
    return bclass;
  }

  private BaseClass getWebAttachmentSearchConfigClass() throws XWikiException {
    XWikiDocument classDoc = getClassDoc(classConf.getWebAttachmentSearchConfigClassRef(
        webUtils.getWikiRef()));
    BaseClass bclass = classDoc.getXClass();
    boolean needsUpdate = classDoc.isNew();
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_MIMETYPES,
        IWebSearchClassConfig.PROPERTY_MIMETYPES, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_MIMETYPES_BLACK_LIST,
        IWebSearchClassConfig.PROPERTY_MIMETYPES_BLACK_LIST, 30);
    needsUpdate |= bclass.addTextField(IWebSearchClassConfig.PROPERTY_FILENAME_PREFIXES,
        IWebSearchClassConfig.PROPERTY_FILENAME_PREFIXES, 30);
    setContentAndSaveClassDocument(classDoc, needsUpdate);
    return bclass;
  }

}
