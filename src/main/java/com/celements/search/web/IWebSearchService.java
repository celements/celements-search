package com.celements.search.web;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.web.packages.WebSearchPackage;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IWebSearchService {

  @NotNull
  Set<WebSearchPackage> getAvailablePackages(@Nullable XWikiDocument configDoc);

  /**
   * @deprecated since 4.5 instead use
   *             {@link #getAvailablePackages(XWikiDocument)}
   */
  @Deprecated
  @NotNull
  Set<WebSearchPackage> getAvailablePackages(@Nullable DocumentReference configDocRef);

  @NotNull
  WebSearchQueryBuilder createWebSearchBuilder(@Nullable XWikiDocument configDoc);

  /**
   * @deprecated since 4.5 instead use
   *             {@link #createWebSearchBuilder(XWikiDocument)}
   */
  @Deprecated
  @NotNull
  WebSearchQueryBuilder createWebSearchBuilder(@Nullable DocumentReference configDocRef)
      throws DocumentNotExistsException;

  @NotNull
  LuceneSearchResult webSearch(@NotNull String searchTerm, @Nullable XWikiDocument configDoc);

  @NotNull
  LuceneSearchResult webSearch(@NotNull String searchTerm, @Nullable XWikiDocument configDoc,
      @NotNull Collection<WebSearchPackage> activatedPackages, @NotNull List<String> languages,
      @NotNull List<String> sortFields);

  /**
   * @deprecated since 4.5 instead use
   *             {@link #webSearch(String, XWikiDocument, Collection, List, List)}
   */
  @Deprecated
  @NotNull
  LuceneSearchResult webSearch(@NotNull String searchTerm,
      @Nullable DocumentReference configDocRef, @Nullable List<WebSearchPackage> activatedPackages,
      @Nullable List<String> languages, @Nullable List<String> sortFields)
      throws DocumentNotExistsException;

}
