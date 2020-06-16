package com.celements.search.lucene.observation;

import static com.celements.logging.LogUtils.*;
import static com.google.common.base.MoreObjects.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
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
    if (isLucenePluginAvailable()) {
      LOGGER.info("queue {}: [{}] with [{}]", event.getClass().getSimpleName(), ref, eventData);
      queue((LuceneQueueEvent) event, ref, firstNonNull(eventData, LuceneQueueEvent.Data.DEFAULT));
    } else {
      LOGGER.warn("LucenePlugin not available, first request?");
    }
  }

  private void queue(LuceneQueueEvent event, EntityReference ref, LuceneQueueEvent.Data eventData) {
    try {
      if (ref instanceof WikiReference) {
        queueWiki((WikiReference) ref, event.isDelete(), eventData);
      } else if (event.isDelete()) {
        queueDelete(ref, eventData);
      } else if (ref instanceof DocumentReference) {
        queueDocument((DocumentReference) ref, eventData);
      } else if (ref instanceof AttachmentReference) {
        queueAttachment((AttachmentReference) ref, eventData);
      } else {
        LOGGER.warn("unable to queue ref [{}]", defer(() -> modelUtils.serializeRef(ref)));
      }
    } catch (DocumentNotExistsException dne) {
      LOGGER.debug("can't queue inexistent document [{}]", modelUtils.serializeRef(ref), dne);
    }
  }

  private void queueWiki(WikiReference wikiRef, boolean delete, LuceneQueueEvent.Data eventData) {
    LOGGER.info("queueWiki: [{}]", defer(() -> modelUtils.serializeRef(wikiRef)));
    getLucenePlugin().queue(newWikiData(wikiRef, delete)
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
  }

  private void queueDocument(DocumentReference docRef, LuceneQueueEvent.Data eventData)
      throws DocumentNotExistsException {
    LOGGER.debug("queueDocument: [{}]", defer(() -> modelUtils.serializeRef(docRef)));
    XWikiDocument doc = modelAccess.getDocument(docRef);
    getLucenePlugin().queue(newDocumentData(doc)
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
  }

  private void queueAttachment(AttachmentReference attRef, LuceneQueueEvent.Data eventData)
      throws DocumentNotExistsException {
    XWikiDocument doc = modelAccess.getDocument(attRef.getDocumentReference());
    queueAttachment(doc.getAttachment(attRef.getName()), eventData);
  }

  private void queueAttachment(XWikiAttachment att, LuceneQueueEvent.Data eventData) {
    LOGGER.debug("queueAttachment: [{}@{}]", defer(() -> modelUtils.serializeRef(
        att.getDoc().getDocumentReference())), att.getFilename());
    getLucenePlugin().queue(newAttachmentData(att)
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
  }

  private void queueDelete(EntityReference ref, LuceneQueueEvent.Data eventData) {
    LOGGER.debug("queueDelete: [{}]", defer(() -> modelUtils.serializeRef(ref)));
    getLucenePlugin().queue(newDeleteData(ref)
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
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
   * doc: 'wiki:space.doc',
   * att: 'wiki:space.doc.file.att.jpg'
   */
  DeleteData newDeleteData(EntityReference ref) {
    String docId = modelUtils.serializeRef(References.extractRef(ref, EntityType.DOCUMENT).or(ref));
    if (ref.getType() == EntityType.ATTACHMENT) {
      docId += ".file." + ref.getName();
    }
    return new DeleteData(docId);
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
