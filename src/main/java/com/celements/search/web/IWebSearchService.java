package com.celements.search.web;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.web.packages.WebSearchPackage;

public interface IWebSearchService {

  public List<WebSearchPackage> getAvailablePackages(DocumentReference configDocRef);

  @NotNull
  public WebSearchQueryBuilder createWebSearchBuilder(@Nullable DocumentReference configDocRef)
      throws DocumentNotExistsException;

  @NotNull
  public LuceneSearchResult webSearch(@NotNull String searchTerm,
      @Nullable DocumentReference configDocRef, @Nullable List<WebSearchPackage> activatedPackages,
      @Nullable List<String> languages, @Nullable List<String> sortFields)
          throws DocumentNotExistsException;

}
