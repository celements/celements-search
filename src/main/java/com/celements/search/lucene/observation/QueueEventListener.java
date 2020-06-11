package com.celements.search.lucene.observation;

import static com.celements.logging.LogUtils.*;
import static com.google.common.base.MoreObjects.*;

import java.util.Arrays;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.AttachmentData;
import com.xpn.xwiki.plugin.lucene.DocumentData;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

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
    return Arrays.asList(new LuceneQueueEvent());
  }

  @Override
  protected void onEventInternal(Event event, EntityReference ref,
      LuceneQueueEvent.Data eventData) {
    if (isLucenePluginAvailable()) {
      LOGGER.info("queue: [{}] with [{}]", ref, eventData);
      queue(ref, firstNonNull(eventData, LuceneQueueEvent.Data.DEFAULT));
    } else {
      LOGGER.warn("LucenePlugin not available, first request?");
    }
  }

  private void queue(EntityReference ref, LuceneQueueEvent.Data eventData) {
    try {
      if (ref instanceof DocumentReference) {
        queueDocumentWithAttachments((DocumentReference) ref, eventData);
      } else if (ref instanceof AttachmentReference) {
        queueAttachment((AttachmentReference) ref, eventData);
      } else {
        LOGGER.warn("unable to queue ref [{}]", defer(() -> modelUtils.serializeRef(ref)));
      }
    } catch (DocumentNotExistsException dne) {
      LOGGER.debug("can't queue inexistend document [{}]", modelUtils.serializeRef(ref), dne);
    }
  }

  private void queueDocumentWithAttachments(DocumentReference docRef,
      LuceneQueueEvent.Data eventData) throws DocumentNotExistsException {
    LOGGER.debug("adding to queue [{}]", defer(() -> modelUtils.serializeRef(docRef)));
    XWikiDocument doc = modelAccess.getDocument(docRef);
    getLucenePlugin().queue(newDocumentData(doc)
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
    doc.getAttachmentList().forEach(att -> queueAttachment(att, eventData));
  }

  private void queueAttachment(AttachmentReference attRef, LuceneQueueEvent.Data eventData)
      throws DocumentNotExistsException {
    XWikiDocument doc = modelAccess.getDocument(attRef.getDocumentReference());
    queueAttachment(doc.getAttachment(attRef.getName()), eventData);
  }

  private void queueAttachment(XWikiAttachment att, LuceneQueueEvent.Data eventData) {
    LOGGER.debug("adding to queue [{}@{}]", defer(() -> modelUtils.serializeRef(
        att.getDoc().getDocumentReference())), att.getFilename());
    getLucenePlugin().queue(newAttachmentData(att)
        .setPriority(eventData.priority)
        .disableObservationEventNotification(eventData.disableEventNotification));
  }

  DocumentData newDocumentData(XWikiDocument doc) {
    return new DocumentData(doc, false);
  }

  AttachmentData newAttachmentData(XWikiAttachment att) {
    return new AttachmentData(att, false);
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
