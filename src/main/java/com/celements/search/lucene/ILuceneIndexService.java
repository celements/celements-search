package com.celements.search.lucene;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
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

  void queue(@NotNull EntityReference ref, @Nullable IndexQueuePriority priority);

  void queueDelete(@NotNull EntityReference ref);

  void queueDelete(@NotNull EntityReference ref, @Nullable IndexQueuePriority priority);

  boolean rebuildIndexForAllWikis();

  boolean rebuildIndex(@Nullable Collection<WikiReference> wikiRefs);

  boolean rebuildIndex(@Nullable EntityReference entityRef);

  boolean rebuildIndexWithWipe();

  void optimizeIndex();

}
