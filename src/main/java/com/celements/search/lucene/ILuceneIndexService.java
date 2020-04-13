package com.celements.search.lucene;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexRebuilder.IndexRebuildFuture;

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
  IndexRebuildFuture rebuildIndex(@Nullable EntityReference entityRef);

  @NotNull
  ImmutableList<IndexRebuildFuture> rebuildIndexForWikiBySpace(@NotNull WikiReference wikiRef);

  @NotNull
  ImmutableList<IndexRebuildFuture> rebuildIndexForAllWikis();

  @NotNull
  ImmutableList<IndexRebuildFuture> rebuildIndexForAllWikisBySpace();

  Optional<IndexRebuildFuture> getCurrentRebuildFuture();

  void optimizeIndex();

}
