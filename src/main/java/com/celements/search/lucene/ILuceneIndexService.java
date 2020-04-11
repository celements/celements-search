package com.celements.search.lucene;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ILuceneIndexService {

  /**
   * @deprecated since 4.0 instead use {@link #queue(EntityReference)}
   */
  @Deprecated
  void queueForIndexing(@NotNull DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException;

  /**
   * @deprecated since 4.0 instead use {@link #queue(EntityReference)}
   */
  @Deprecated
  void queueForIndexing(@NotNull XWikiDocument doc);

  void queue(@NotNull EntityReference ref);

  @NotNull
  CompletableFuture<Long> rebuildIndex(@Nullable EntityReference entityRef);

  @NotNull
  ImmutableMap<SpaceReference, CompletableFuture<Long>> rebuildIndexForWikiBySpace(
      @NotNull WikiReference wikiRef);

  @NotNull
  ImmutableMap<SpaceReference, CompletableFuture<Long>> rebuildIndexForAllWikisBySpace();

  @NotNull
  ImmutableMap<WikiReference, CompletableFuture<Long>> rebuildIndexForAllWikis();

  Optional<CompletableFuture<Long>> getLatestRebuildFuture();

  void optimizeIndex();

}
