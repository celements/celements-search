package com.celements.search.lucene;

import static com.celements.common.MoreObjectsCel.*;
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
import org.xwiki.observation.ObservationManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.model.util.References;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.index.rebuild.LuceneIndexRebuildService;
import com.celements.search.lucene.index.rebuild.LuceneIndexRebuildService.IndexRebuildFuture;
import com.celements.search.lucene.observation.LuceneQueueEvent;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

@Component
public class LuceneIndexService implements ILuceneIndexService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneIndexService.class);

  public static final String EXEC_QUEUE_PRIORITY = "lucene.index.queue.priority";
  public static final String EXEC_DISABLE_EVENT_NOTIFICATION = "lucene.index.disableEventNotification";

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
    queue(ref, null, null);
  }

  @Override
  public void queue(EntityReference ref, IndexQueuePriority priority) {
    queue(ref, priority, null);
  }

  @Override
  public void queueWithoutNotifications(EntityReference ref) {
    queue(ref, null, true);
  }

  @Override
  public void queueWithoutNotifications(EntityReference ref, IndexQueuePriority priority) {
    queue(ref, priority, true);
  }

  private void queue(EntityReference ref, IndexQueuePriority priority,
      Boolean disableEventNotification) {
    if (ref != null) {
      priority = Optional.ofNullable(priority)
          .orElseGet(() -> getExecutionParam(EXEC_QUEUE_PRIORITY, IndexQueuePriority.class)
          .orElse(LuceneQueueEvent.Data.DEFAULT.priority));
      disableEventNotification = Optional.ofNullable(disableEventNotification)
          .orElseGet(() -> getExecutionParam(EXEC_DISABLE_EVENT_NOTIFICATION, Boolean.class)
          .orElse(LuceneQueueEvent.Data.DEFAULT.disableEventNotification));
      LuceneQueueEvent event = new LuceneQueueEvent();
      getObservationManager().notify(event, ref,
          new LuceneQueueEvent.Data(priority, disableEventNotification));
    }
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
    EntityReference filterRef = Optional.ofNullable(ref).map(References::cloneRef)
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

  private <T> Optional<T> getExecutionParam(String key, Class<T> type) {
    return tryCast(execution.getContext().getProperty(key), type);
  }

  /**
   * loaded lazily due to cyclic dependency
   */
  private ObservationManager getObservationManager() {
    return Utils.getComponent(ObservationManager.class);
  }

}
