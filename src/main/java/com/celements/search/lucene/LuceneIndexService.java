package com.celements.search.lucene;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.ObservationManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.search.lucene.index.LuceneDocId;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
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
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
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
    for (XWikiAttachment attach : doc.getAttachmentList()) {
      queue(RefBuilder.from(doc.getDocumentReference())
          .with(EntityType.ATTACHMENT, attach.getFilename())
          .build(AttachmentReference.class));
    }
  }

  @Override
  public void queue(EntityReference ref) {
    if (ref != null) {
      getObservationManager().notify(
          new LuceneQueueIndexEvent(),
          new LuceneDocId(ref),
          null);
    }
  }

  @Override
  public void queue(EntityReference ref, IndexQueuePriority priority) {
    if (ref != null) {
      getObservationManager().notify(
          new LuceneQueueIndexEvent(priority),
          new LuceneDocId(ref),
          null);
    }
  }

  @Override
  public void queue(DocumentReference docRef, String lang) {
    if (docRef != null) {
      getObservationManager().notify(
          new LuceneQueueIndexEvent(),
          new LuceneDocId(docRef, lang),
          null);
    }
  }

  @Override
  public void queue(DocumentReference docRef, String lang, IndexQueuePriority priority) {
    if (docRef != null) {
      getObservationManager().notify(
          new LuceneQueueIndexEvent(priority),
          new LuceneDocId(docRef, lang),
          null);
    }
  }

  @Override
  public void queueDelete(EntityReference ref) {
    if (ref != null) {
      getObservationManager().notify(
          new LuceneQueueDeleteEvent(),
          new LuceneDocId(ref),
          null);
    }
  }

  @Override
  public void queueDelete(EntityReference ref, IndexQueuePriority priority) {
    if (ref != null) {
      getObservationManager().notify(
          new LuceneQueueDeleteEvent(priority),
          new LuceneDocId(ref),
          null);
    }
  }

  @Override
  public void queueDelete(@NotNull DocumentReference docRef, @NotNull String lang) {
    if (docRef != null) {
      getObservationManager().notify(
          new LuceneQueueDeleteEvent(),
          new LuceneDocId(docRef, lang),
          null);
    }
  }

  @Override
  public void queueDelete(@NotNull DocumentReference docRef, @NotNull String lang,
      IndexQueuePriority priority) {
    if (docRef != null) {
      getObservationManager().notify(
          new LuceneQueueDeleteEvent(priority),
          new LuceneDocId(docRef, lang),
          null);
    }
  }

  @Override
  public boolean rebuildIndexForAllWikis() {
    LOGGER.info("rebuildIndexForAllWikis start");
    return getLucenePlugin().rebuildIndex();
  }

  @Override
  public boolean rebuildIndex(Collection<WikiReference> wikiRefs) {
    LOGGER.info("rebuildIndex start for wikiRefs '{}'", wikiRefs);
    return getLucenePlugin().rebuildIndex(new ArrayList<>(wikiRefs), false);
  }

  @Override
  public boolean rebuildIndex(EntityReference entityRef) {
    LOGGER.info("rebuildIndex start for entityRef '{}'", entityRef);
    return getLucenePlugin().rebuildIndex(entityRef, false);
  }

  @Override
  public boolean rebuildIndexWithWipe() {
    return getLucenePlugin().rebuildIndexWithWipe(null, false);
  }

  @Override
  public void optimizeIndex() {
    getLucenePlugin().optimizeIndex();
  }

  private LucenePlugin getLucenePlugin() {
    return (LucenePlugin) getContext().getWiki().getPlugin("lucene", getContext());
  }

  /**
   * loaded lazily due to cyclic dependency
   */
  private ObservationManager getObservationManager() {
    return Utils.getComponent(ObservationManager.class);
  }

}
