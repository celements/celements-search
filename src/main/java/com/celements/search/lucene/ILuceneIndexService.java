package com.celements.search.lucene;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.index.rebuild.LuceneIndexRebuildService.IndexRebuildFuture;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ILuceneIndexService {

  long getIndexSize();

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

  void queue(@NotNull EntityReference ref, @Nullable IndexQueuePriority priority);

  void queueWithoutNotifications(@NotNull EntityReference ref);

  void queueWithoutNotifications(@NotNull EntityReference ref,
      @Nullable IndexQueuePriority priority);

  long getQueueSize();

  long getQueueSize(@NotNull IndexQueuePriority priority);

  @NotNull
  IndexRebuildFuture rebuildIndex(@Nullable EntityReference ref);

  @NotNull
  ImmutableList<IndexRebuildFuture> rebuildIndexForWikiBySpace(@Nullable WikiReference wikiRef);

  @NotNull
  ImmutableList<IndexRebuildFuture> rebuildIndexForAllWikis();

  @NotNull
  ImmutableList<IndexRebuildFuture> rebuildIndexForAllWikisBySpace();

  void optimizeIndex();

}
