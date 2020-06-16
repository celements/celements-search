package com.celements.search.lucene.observation;

import static com.celements.common.MoreObjectsCel.*;
import static com.celements.logging.LogUtils.*;
import static com.google.common.base.MoreObjects.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.celements.model.util.References;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.AbstractIndexData;
import com.xpn.xwiki.plugin.lucene.AttachmentData;
import com.xpn.xwiki.plugin.lucene.DeleteData;
import com.xpn.xwiki.plugin.lucene.DocumentData;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.plugin.lucene.WikiData;

@Component(QueueEventListener.NAME)
public class QueueEventListener
    extends AbstractRemoteEventListener<EntityReference, LuceneQueueEvent.Data> {

  public static final String NAME = "celements.search.QueueEventListener";

  @Requirement
  private ModelUtils modelUtils;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return ImmutableList.of(
        new LuceneQueueIndexEvent(),
        new LuceneQueueDeleteEvent());
  }

  @Override
  protected void onEventInternal(Event event, EntityReference ref,
      LuceneQueueEvent.Data eventData) {
    try {
      LuceneQueueEvent queueEvent = (LuceneQueueEvent) event;
      AbstractIndexData indexData = null;
      if (ref instanceof WikiReference) {
        indexData = newWikiData((WikiReference) ref, queueEvent.isDelete());
      } else if (queueEvent.isDelete()) {
        indexData = newDeleteData(ref);
      } else if (ref instanceof QueueLangDocumentReference) {
        QueueLangDocumentReference langDocRef = (QueueLangDocumentReference) ref;
        indexData = newDocumentData(modelAccess.getDocument(langDocRef, langDocRef.getLang()
            .orElse(null)));
      } else if (ref instanceof AttachmentReference) {
        AttachmentReference attRef = (AttachmentReference) ref;
        indexData = newAttachmentData(modelAccess.getDocument(attRef.getDocumentReference())
            .getAttachment(attRef.getName()));
      } else {
        LOGGER.warn("unable to queue ref [{}]", defer(() -> modelUtils.serializeRef(ref)));
      }
      queue(indexData, firstNonNull(eventData, LuceneQueueEvent.Data.DEFAULT));
    } catch (DocumentNotExistsException dne) {
      LOGGER.debug("can't queue inexistent document [{}]", modelUtils.serializeRef(ref), dne);
    }
  }

  private void queue(AbstractIndexData indexData, LuceneQueueEvent.Data eventData) {
    if (indexData != null) {
      indexData.setPriority(eventData.priority);
      indexData.disableObservationEventNotification(eventData.disableEventNotification);
      LOGGER.info("queue: {}", indexData);
      if (isLucenePluginAvailable()) {
        getLucenePlugin().queue(indexData);
      } else {
        LOGGER.warn("LucenePlugin not available, first request?");
      }
    }
  }

  WikiData newWikiData(WikiReference wiki, boolean delete) {
    return new WikiData(wiki, delete);
  }

  DocumentData newDocumentData(XWikiDocument doc) {
    return new DocumentData(doc, false);
  }

  AttachmentData newAttachmentData(XWikiAttachment att) {
    return new AttachmentData(att, false);
  }

  /**
   * docId for
   * doc: 'wiki:space.doc.en',
   * att: 'wiki:space.doc.file.att.jpg'
   */
  DeleteData newDeleteData(EntityReference ref) {
    StringBuilder docId = new StringBuilder();
    docId.append(modelUtils.serializeRef(References.extractRef(ref, EntityType.DOCUMENT).or(ref)));
    tryCast(ref, QueueLangDocumentReference.class).ifPresent(
        langRef -> docId.append('.').append(langRef.getLang().orElse("default")));
    if (ref.getType() == EntityType.ATTACHMENT) {
      docId.append(".file.").append(ref.getName());
    }
    return new DeleteData(docId.toString());
  }

  private boolean isLucenePluginAvailable() {
    try {
      return (getLucenePlugin() != null);
    } catch (NullPointerException npe) {
      return false;
    }
  }

  private LucenePlugin getLucenePlugin() {
    return (LucenePlugin) context.getXWikiContext().getWiki().getPlugin(
        "lucene", context.getXWikiContext());
  }

}
