package com.celements.search.web;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.web.packages.WebSearchPackage;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface WebSearchQueryBuilder {

  @NotNull
  WikiReference getWikiRef();

  @NotNull
  WebSearchQueryBuilder setWikiRef(@Nullable WikiReference wikiRef);

  @NotNull
  DocumentReference getConfigDocRef();

  @NotNull
  WebSearchQueryBuilder setConfigDoc(@Nullable XWikiDocument doc);

  @NotNull
  String getSearchTerm();

  @NotNull
  WebSearchQueryBuilder setSearchTerm(@NotNull String searchTerm);

  @NotNull
  Collection<WebSearchPackage> getPackages();

  @NotNull
  WebSearchQueryBuilder addPackage(@NotNull WebSearchPackage searchPackage);

  @NotNull
  WebSearchQueryBuilder addPackage(@NotNull String packageName);

  @NotNull
  LuceneQuery build();

}
