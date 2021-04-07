package com.celements.search.web;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.packages.WebSearchPackage;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component
public class WebSearchService implements IWebSearchService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSearchService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ILuceneSearchService luceneSearchService;

  @Requirement
  private List<WebSearchPackage> webSearchPackages;

  @Requirement
  private ModelContext context;

  @Deprecated
  @Override
  public Set<WebSearchPackage> getAvailablePackages(DocumentReference configDocRef) {
    return getAvailablePackages(modelAccess.getOrCreateDocument(configDocRef));
  }

  @Override
  public Set<WebSearchPackage> getAvailablePackages(XWikiDocument configDoc) {
    Set<WebSearchPackage> searchPackages = new LinkedHashSet<>();
    if (configDoc == null) {
      getDefaultPackages().forEach(searchPackages::add);
    } else {
      getConfiguredPackages(configDoc).forEach(searchPackages::add);
      if (searchPackages.isEmpty()) {
        getDefaultPackages().forEach(searchPackages::add);
      }
      getRequiredPackages(configDoc).forEach(searchPackages::add);
    }
    return searchPackages;
  }

  private Stream<WebSearchPackage> getConfiguredPackages(XWikiDocument configDoc) {
    return XWikiObjectFetcher.on(configDoc).fetchField(WebSearchConfigClass.FIELD_PACKAGES)
        .stream().flatMap(List::stream);
  }

  private Stream<WebSearchPackage> getDefaultPackages() {
    return webSearchPackages.stream().filter(WebSearchPackage::isDefault);
  }

  private Stream<WebSearchPackage> getRequiredPackages(final XWikiDocument configDoc) {
    return webSearchPackages.stream().filter(pack -> pack.isRequired(configDoc));
  }

  @Deprecated
  @Override
  public WebSearchQueryBuilder createWebSearchBuilder(DocumentReference configDocRef)
      throws DocumentNotExistsException {
    return createWebSearchBuilder(modelAccess.getDocument(configDocRef));
  }

  @Override
  public WebSearchQueryBuilder createWebSearchBuilder(XWikiDocument configDoc) {
    WebSearchQueryBuilder ret = null;
    ret = Utils.getComponent(WebSearchQueryBuilder.class);
    ret.setConfigDoc(configDoc);
    return ret;
  }

  @Override
  public LuceneSearchResult webSearch(String searchTerm, XWikiDocument configDoc) {
    return webSearch(searchTerm, configDoc, ImmutableList.of(), ImmutableList.of(),
        ImmutableList.of());
  }

  @Deprecated
  @Override
  public LuceneSearchResult webSearch(String searchTerm, DocumentReference configDocRef,
      List<WebSearchPackage> activatedPackages, List<String> languages, List<String> sortFields)
      throws DocumentNotExistsException {
    return webSearch(searchTerm, modelAccess.getOrCreateDocument(configDocRef), activatedPackages,
        languages, sortFields);
  }

  @Override
  public LuceneSearchResult webSearch(String searchTerm, XWikiDocument configDoc,
      Collection<WebSearchPackage> activatedPackages, List<String> languages,
      List<String> sortFields) {
    WebSearchQueryBuilder builder = createWebSearchBuilder(configDoc);
    builder.setSearchTerm(searchTerm);
    activatedPackages.stream().forEach(builder::addPackage);
    if (configDoc != null) {
      XWikiObjectFetcher.on(configDoc).fetchField(WebSearchConfigClass.FIELD_SORT_FIELDS)
          .stream().flatMap(List::stream)
          .forEach(sortFields::add);
    }
    LuceneQuery query = builder.build();
    return luceneSearchService.search(query, sortFields, languages);
  }

}
