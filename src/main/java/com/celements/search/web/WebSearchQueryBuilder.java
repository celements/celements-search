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

  public @NotNull WikiReference getWikiRef();

  public @NotNull WebSearchQueryBuilder setWikiRef(@Nullable WikiReference wikiRef);

  public @NotNull DocumentReference getConfigDocRef();

  public @NotNull WebSearchQueryBuilder setConfigDoc(@Nullable XWikiDocument doc);

  public @NotNull String getSearchTerm();

  public @NotNull WebSearchQueryBuilder setSearchTerm(@NotNull String searchTerm);

  public @NotNull Collection<WebSearchPackage> getPackages();

  public @NotNull WebSearchQueryBuilder addPackage(@NotNull WebSearchPackage searchPackage);

  public @NotNull WebSearchQueryBuilder addPackage(@NotNull String packageName);

  public @NotNull LuceneQuery build();

}
