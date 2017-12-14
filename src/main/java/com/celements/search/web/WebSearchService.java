package com.celements.search.web;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

@Component
public class WebSearchService implements IWebSearchService {

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private WebSearchQueryBuilder queryBuilder;

  @Requirement
  ILuceneSearchService luceneSearchService;

  @Requirement
  private List<WebSearchPackage> webSearchPackages;

  @Requirement
  private ModelContext context;

  @Override
  public List<WebSearchPackage> getAvailablePackages(DocumentReference configDocRef) {
    List<WebSearchPackage> ret = new ArrayList<>();
    ret.addAll(getConfiguredPackages());
    if (ret.isEmpty()) {
      ret.addAll(getDefaultPackages());
    }
    ret.addAll(getRequiredPackages());
    checkState(!ret.isEmpty(), "no WebSearchPackages defined");
    return ret;
  }

  private List<WebSearchPackage> getConfiguredPackages() {
    return modelAccess.getFieldValue(queryBuilder.getConfigDoc(),
        WebSearchConfigClass.FIELD_PACKAGES).orNull();
  }

  private List<WebSearchPackage> getDefaultPackages() {
    return FluentIterable.from(webSearchPackages).filter(
        WebSearchPackage.PREDICATE_DEFAULT).toList();
  }

  private List<WebSearchPackage> getRequiredPackages() {
    return FluentIterable.from(webSearchPackages).filter(new Predicate<WebSearchPackage>() {

      @Override
      public boolean apply(WebSearchPackage searchPackage) {
        return searchPackage.isRequired(queryBuilder.getConfigDoc());
      }
    }).toList();
  }

  @Override
  public WebSearchQueryBuilder createWebSearchBuilder(DocumentReference configDocRef)
      throws DocumentNotExistsException {
    if ((configDocRef != null) && rightsAccess.hasAccessLevel(configDocRef, EAccessLevel.VIEW)) {
      queryBuilder.setConfigDoc(modelAccess.getDocument(configDocRef));
    }
    return queryBuilder;
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
