package com.celements.search.lucene;

import static com.google.common.collect.ImmutableMap.*;
import static java.util.function.Function.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.ObservationManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.search.lucene.observation.LuceneQueueEvent;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

@Component
public class LuceneIndexService implements ILuceneIndexService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneIndexService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

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
    if (ref != null) {
      getObservationManager().notify(new LuceneQueueEvent(), ref, null);
    }
  }

  @Override
  public CompletableFuture<Long> rebuildIndex(final EntityReference entityRef) {
    LOGGER.info("rebuildIndex - start [{}]", entityRef);
    return getLucenePlugin().rebuildIndex(entityRef);
  }

  @Override
  public ImmutableMap<SpaceReference, CompletableFuture<Long>> rebuildIndexForWikiBySpace(
      WikiReference wikiRef) {
    return modelUtils.getAllSpaces(wikiRef).collect(toImmutableMap(identity(), this::rebuildIndex));
  }

  @Override
  public ImmutableMap<SpaceReference, CompletableFuture<Long>> rebuildIndexForAllWikisBySpace() {
    return modelUtils.getAllWikis().flatMap(modelUtils::getAllSpaces)
        .collect(toImmutableMap(identity(), this::rebuildIndex));
  }

  @Override
  public ImmutableMap<WikiReference, CompletableFuture<Long>> rebuildIndexForAllWikis() {
    return modelUtils.getAllWikis().collect(toImmutableMap(identity(), this::rebuildIndex));
  }

  @Override
  public Optional<CompletableFuture<Long>> getLatestRebuildFuture() {
    return getLucenePlugin().getLatestRebuildFuture();
  }

  @Override
  public void optimizeIndex() {
    getLucenePlugin().optimizeIndex();
  }

  private LucenePlugin getLucenePlugin() {
    return (LucenePlugin) getXContext().getWiki().getPlugin("lucene", getXContext());
  }

  private XWikiContext getXContext() {
    return context.getXWikiContext();
  }

  /**
   * loaded lazily due to cyclic dependency
   */
  private ObservationManager getObservationManager() {
    return Utils.getComponent(ObservationManager.class);
  }

}
