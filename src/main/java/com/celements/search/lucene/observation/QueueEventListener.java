package com.celements.search.lucene.observation;

import static com.celements.logging.LogUtils.*;
import static com.google.common.base.MoreObjects.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.AttachmentData;
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
    if (isLucenePluginAvailable()) {
      LOGGER.info("queue: [{}] with [{}]", ref, eventData);
      queue((LuceneQueueEvent) event, ref, firstNonNull(eventData, LuceneQueueEvent.Data.DEFAULT));
    } else {
      LOGGER.warn("LucenePlugin not available, first request?");
    }
  }

  private void queue(LuceneQueueEvent event, EntityReference ref, LuceneQueueEvent.Data eventData) {
    try {
      if (ref instanceof DocumentReference) {
        queueDocumentWithAttachments(event, (DocumentReference) ref, eventData);
      } else if (ref instanceof AttachmentReference) {
        queueAttachment(event, (AttachmentReference) ref, eventData);
      } else if (ref instanceof WikiReference) {
        queueWiki(event, (WikiReference) ref, eventData);
      } else {
        LOGGER.warn("unable to queue ref [{}]", defer(() -> modelUtils.serializeRef(ref)));
      }
    } catch (DocumentNotExistsException dne) {
      LOGGER.debug("can't queue inexistend document [{}]", modelUtils.serializeRef(ref), dne);
    }
  }

  private void queueDocumentWithAttachments(LuceneQueueEvent event, DocumentReference docRef,
      LuceneQueueEvent.Data eventData) throws DocumentNotExistsException {
    LOGGER.debug("adding to queue [{}]", defer(() -> modelUtils.serializeRef(docRef)));
    XWikiDocument doc = modelAccess.getDocument(docRef);
    getLucenePlugin().queue(newDocumentData(doc, event.isDelete())
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
    doc.getAttachmentList().forEach(att -> queueAttachment(event, att, eventData));
  }

  private void queueAttachment(LuceneQueueEvent event, AttachmentReference attRef,
      LuceneQueueEvent.Data eventData)
      throws DocumentNotExistsException {
    XWikiDocument doc = modelAccess.getDocument(attRef.getDocumentReference());
    queueAttachment(event, doc.getAttachment(attRef.getName()), eventData);
  }

  private void queueAttachment(LuceneQueueEvent event, XWikiAttachment att,
      LuceneQueueEvent.Data eventData) {
    LOGGER.debug("adding to queue [{}@{}]", defer(() -> modelUtils.serializeRef(
        att.getDoc().getDocumentReference())), att.getFilename());
    getLucenePlugin().queue(newAttachmentData(att, event.isDelete())
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
  }

  private void queueWiki(LuceneQueueEvent event, WikiReference wikiRef,
      LuceneQueueEvent.Data eventData) {
    LOGGER.debug("adding to queue [{}]", defer(() -> modelUtils.serializeRef(wikiRef)));
    getLucenePlugin().queue(newWikiData(wikiRef, event.isDelete())
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
  }

  DocumentData newDocumentData(XWikiDocument doc, boolean delete) {
    return new DocumentData(doc, delete);
  }

  AttachmentData newAttachmentData(XWikiAttachment att, boolean delete) {
    return new AttachmentData(att, delete);
  }

  WikiData newWikiData(WikiReference wiki, boolean delete) {
    return new WikiData(wiki, delete);
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
