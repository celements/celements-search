package com.celements.search.lucene;

import static com.celements.logging.LogUtils.*;
import static com.google.common.collect.ImmutableList.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
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

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Requirement
  private LuceneIndexRebuildService rebuildService;

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
