package com.celements.search.lucene;

import static com.celements.logging.LogUtils.*;
import static com.google.common.collect.ImmutableList.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.index.queue.QueueTask;
import com.celements.search.lucene.index.rebuild.LuceneIndexRebuildService;
import com.celements.search.lucene.index.rebuild.LuceneIndexRebuildService.IndexRebuildFuture;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

@Component
public class LuceneIndexService implements ILuceneIndexService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneIndexService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private Execution execution;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Requirement
  private LuceneIndexRebuildService rebuildService;

  @Override
  public long getIndexSize() {
    return getLucenePlugin().map(LucenePlugin::getLuceneDocCount).orElse(-1L);
  }

  @Override
  @Deprecated
  public void queueForIndexing(DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException {
    queue(docRef);
  }

  @Override
  @Deprecated
  public void queueForIndexing(XWikiDocument doc) {
    queue(doc.getDocumentReference());
  }

  @Override
  public void queue(EntityReference ref) {
    indexTask(ref).queue();
  }

  @Override
  public QueueTask indexTask(EntityReference ref) {
    return new QueueTask(ref, new LuceneQueueIndexEvent());
  }

  @Override
  public QueueTask deleteTask(EntityReference ref) {
    return new QueueTask(ref, new LuceneQueueDeleteEvent());
  }

  @Override
  public long getQueueSize() {
    return getLucenePlugin().map(LucenePlugin::getQueueSize).orElse(-1L);
  }

  @Override
  public long getQueueSize(IndexQueuePriority priority) {
    return getLucenePlugin().map(p -> p.getQueueSize(priority)).orElse(-1L);
  }

  @Override
  public IndexRebuildFuture rebuildIndex(EntityReference ref) {
    EntityReference filterRef = Optional.ofNullable(ref)
        .orElseGet(context::getWikiRef);
    LOGGER.info("rebuildIndex - start [{}]", defer(() -> modelUtils.serializeRef(filterRef)));
    return rebuildService.startIndexRebuild(filterRef);
  }

  @Override
  public ImmutableList<IndexRebuildFuture> rebuildIndexForWikiBySpace(WikiReference wikiRef) {
    wikiRef = Optional.ofNullable(wikiRef).orElseGet(context::getWikiRef);
    return modelUtils.getAllSpaces(wikiRef).map(this::rebuildIndex).collect(toImmutableList());
  }

  @Override
  public ImmutableList<IndexRebuildFuture> rebuildIndexForAllWikis() {
    return modelUtils.getAllWikis().map(this::rebuildIndex).collect(toImmutableList());
  }

  @Override
  public ImmutableList<IndexRebuildFuture> rebuildIndexForAllWikisBySpace() {
    return modelUtils.getAllWikis().flatMap(modelUtils::getAllSpaces).map(this::rebuildIndex)
        .collect(toImmutableList());
  }

  @Override
  public void optimizeIndex() {
    getLucenePlugin().ifPresent(LucenePlugin::optimizeIndex);
  }

  private Optional<LucenePlugin> getLucenePlugin() {
    try {
      return Optional.of((LucenePlugin) getXContext().getWiki().getPlugin("lucene", getXContext()));
    } catch (NullPointerException npe) {
      LOGGER.warn("LucenePlugin not available, first request?");
      return Optional.empty();
    }
  }

  private XWikiContext getXContext() {
    return context.getXWikiContext();
  }

}
