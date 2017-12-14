package com.celements.search.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.packages.WebSearchPackage;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component
public class WebSearchService implements IWebSearchService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSearchService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private ILuceneSearchService luceneSearchService;

  @Requirement
  private List<WebSearchPackage> webSearchPackages;

  @Requirement
  private ModelContext context;

  @Override
  public Set<WebSearchPackage> getAvailablePackages(DocumentReference configDocRef) {
    return getAvailablePackages(configDocRef, new LinkedHashSet<WebSearchPackage>());
  }

  @Override
  public Set<WebSearchPackage> getAvailablePackages(DocumentReference configDocRef,
      Set<WebSearchPackage> searchPackages) {
    try {
      searchPackages.addAll(getConfiguredPackages(configDocRef));
      if (searchPackages.isEmpty()) {
        searchPackages.addAll(getDefaultPackages(configDocRef));
      }
      searchPackages.addAll(getRequiredPackages(configDocRef));
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("Failed to load configDoc '{}'", configDocRef, exp);
    }
    return searchPackages;
  }

  private List<WebSearchPackage> getConfiguredPackages(DocumentReference configDocRef)
      throws DocumentNotExistsException {
    return modelAccess.getFieldValue(modelAccess.getDocument(configDocRef),
        WebSearchConfigClass.FIELD_PACKAGES).orNull();
  }

  private List<WebSearchPackage> getDefaultPackages(DocumentReference configDocRef) {
    return FluentIterable.from(webSearchPackages).filter(
        WebSearchPackage.PREDICATE_DEFAULT).toList();
  }

  private List<WebSearchPackage> getRequiredPackages(DocumentReference configDocRef)
      throws DocumentNotExistsException {
    final XWikiDocument configDoc = modelAccess.getDocument(configDocRef);

    return FluentIterable.from(webSearchPackages).filter(new Predicate<WebSearchPackage>() {

      @Override
      public boolean apply(WebSearchPackage searchPackage) {
        return searchPackage.isRequired(configDoc);
      }
    }).toList();
  }

  @Override
  public WebSearchQueryBuilder createWebSearchBuilder(DocumentReference configDocRef)
      throws DocumentNotExistsException {
    WebSearchQueryBuilder ret = null;
    ret = Utils.getComponent(WebSearchQueryBuilder.class);
    if ((configDocRef != null) && rightsAccess.hasAccessLevel(configDocRef, EAccessLevel.VIEW)) {
      ret.setConfigDoc(modelAccess.getDocument(configDocRef));
    }
    return ret;
  }

  @Override
  public LuceneSearchResult webSearch(String searchTerm, DocumentReference configDocRef,
      List<WebSearchPackage> activatedPackages, List<String> languages, List<String> sortFields)
          throws DocumentNotExistsException {
    WebSearchQueryBuilder builder = createWebSearchBuilder(configDocRef);
    builder.setSearchTerm(searchTerm);
    LuceneQuery query = builder.build();
    for (WebSearchPackage searchPackage : activatedPackages) {
      builder.addPackage(searchPackage);
    }
    if (sortFields == null) {
      sortFields = new ArrayList<String>();
    }
    sortFields.addAll(modelAccess.getFieldValue(builder.getConfigDocRef(),
        WebSearchConfigClass.FIELD_SORT_FIELDS).or(Collections.<String>emptyList()));
    return luceneSearchService.search(query, sortFields, languages);
  }

}
