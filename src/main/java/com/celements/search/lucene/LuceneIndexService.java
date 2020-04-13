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
import com.celements.search.lucene.observation.LuceneQueueEvent;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexRebuilder.IndexRebuildFuture;
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
  public IndexRebuildFuture rebuildIndex(final EntityReference entityRef) {
    LOGGER.info("rebuildIndex - start [{}]", defer(() -> modelUtils.serializeRef(entityRef)));
    return getLucenePlugin().rebuildIndex(entityRef);
  }

  @Override
  public ImmutableList<IndexRebuildFuture> rebuildIndexForWikiBySpace(WikiReference wikiRef) {
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
  public Optional<IndexRebuildFuture> getCurrentRebuildFuture() {
    return getLucenePlugin().getCurrentRebuildFuture();
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
