package com.celements.search.web;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.web.packages.WebSearchPackage;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IWebSearchService {

  @NotNull
  Set<WebSearchPackage> getAvailablePackages(@Nullable XWikiDocument configDoc);

  @NotNull
  WebSearchQueryBuilder createWebSearchBuilder(@Nullable XWikiDocument configDoc);

  @NotNull
  LuceneSearchResult webSearch(@NotNull String searchTerm, @Nullable XWikiDocument configDoc);

  @NotNull
  LuceneSearchResult webSearch(@NotNull String searchTerm, @Nullable XWikiDocument configDoc,
      @NotNull Collection<WebSearchPackage> activatedPackages, @NotNull List<String> languages,
      @NotNull List<String> sortFields);

}
