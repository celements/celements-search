package com.celements.search.web;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.web.packages.WebSearchPackage;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component(WebSearchScriptService.NAME)
public class WebSearchScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSearchScriptService.class);

  public static final String NAME = "websearch";

  @Requirement
  private IWebSearchService searchService;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private IModelAccessFacade modelAccess;

  public WebSearchQueryBuilder createWebSearchBuilder(DocumentReference configDocRef) {
    WebSearchQueryBuilder ret = null;
    try {
      if ((configDocRef != null) && rightsAccess.hasAccessLevel(configDocRef, EAccessLevel.VIEW)) {
        ret = searchService.createWebSearchBuilder(modelAccess.getDocument(configDocRef));
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("createWebSearchBuilder: provided configDoc '{}' doesn't exist", configDocRef);
    }
    LOGGER.debug("createWebSearchBuilder: returning '{}'", ret);
    return ret;
  }

  public LuceneSearchResult webSearch(String searchTerm, DocumentReference configDocRef,
      List<String> languages) {
    return webSearch(searchTerm, configDocRef, languages, null);
  }

  public LuceneSearchResult webSearch(String searchTerm, DocumentReference configDocRef,
      List<String> languages, List<String> sortFields) {
    return webSearch(searchTerm, configDocRef, null, languages, sortFields);
  }

  public LuceneSearchResult webSearch(String searchTerm, DocumentReference configDocRef,
      List<String> activatedPackageNames, List<String> languages, List<String> sortFields) {
    LuceneSearchResult ret = null;
    if ((configDocRef != null) && rightsAccess.hasAccessLevel(configDocRef, EAccessLevel.VIEW)) {
      List<WebSearchPackage> activatedPackages = new ArrayList<>();
      if (activatedPackageNames != null) {
        for (String packageName : activatedPackageNames) {
          try {
            activatedPackages.add(Utils.getComponentManager().lookup(WebSearchPackage.class,
                packageName));
          } catch (ComponentLookupException exc) {
            LOGGER.info("addPackage: invalid package '{}'", packageName);
          }
        }
      }
      languages = (languages != null) ? languages : new ArrayList<>();
      sortFields = (sortFields != null) ? sortFields : new ArrayList<>();
      try {
        ret = searchService.webSearch(searchTerm, modelAccess.getDocument(configDocRef),
            activatedPackages, languages, sortFields);
      } catch (DocumentNotExistsException exc) {
        LOGGER.info("webSearch: provided configDoc '{}' doesn't exist", configDocRef);
      }
      LOGGER.debug("webSearch: returning '{}'", ret);
    }
    return ret;
  }

  public List<String> getAvailablePackages(DocumentReference configDocRef) {
    XWikiDocument configDoc = (configDocRef == null) ? null
        : modelAccess.getOrCreateDocument(configDocRef);
    return searchService.getAvailablePackages(configDoc).stream()
        .map(WebSearchPackage::getName)
        .collect(Collectors.toList());
  }

}
