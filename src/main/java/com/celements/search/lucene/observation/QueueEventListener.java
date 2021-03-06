package com.celements.search.lucene.observation;

import static com.celements.common.MoreObjectsCel.*;
import static com.celements.logging.LogUtils.*;
import static com.google.common.base.MoreObjects.*;

import java.util.List;
import java.util.Optional;

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
    LuceneQueueEvent queueEvent = (LuceneQueueEvent) event;
    AbstractIndexData indexData = null;
    if (ref instanceof WikiReference) {
      indexData = newWikiData((WikiReference) ref, queueEvent.isDelete());
    } else if (queueEvent.isDelete()) {
      indexData = newDeleteData(ref);
    } else if (ref instanceof DocumentReference) {
      indexData = newDocumentData((DocumentReference) ref);
    } else if (ref instanceof AttachmentReference) {
      indexData = newAttachmentData((AttachmentReference) ref);
    } else {
      LOGGER.warn("unable to queue ref [{}]", defer(() -> modelUtils.serializeRef(ref)));
    }
    queue(indexData, firstNonNull(eventData, LuceneQueueEvent.Data.DEFAULT));
  }

  private void queue(AbstractIndexData indexData, LuceneQueueEvent.Data eventData) {
    if (indexData != null) {
      indexData.setPriority(eventData.priority);
      indexData.setDisableObservationEventNotification(eventData.disableEventNotification);
      if (isLucenePluginAvailable()) {
        LOGGER.debug("queue: {}", indexData);
        getLucenePlugin().queue(indexData);
      } else {
        LOGGER.warn("LucenePlugin not available, first request? [{}]", indexData);
      }
    }
  }

  WikiData newWikiData(WikiReference wiki, boolean delete) {
    return new WikiData(wiki, delete);
  }

  private AbstractIndexData newDocumentData(DocumentReference docRef) {
    try {
      return newDocumentData(modelAccess.getDocument(docRef, tryGetLang(docRef).orElse(null)));
    } catch (DocumentNotExistsException dne) {
      LOGGER.debug("can't queue inexistent document [{}]",
          defer(() -> modelUtils.serializeRef(docRef)));
      return null;
    }
  }

  DocumentData newDocumentData(XWikiDocument doc) {
    return new DocumentData(doc, false);
  }

  private AbstractIndexData newAttachmentData(AttachmentReference attRef) {
    XWikiAttachment att = modelAccess.getOrCreateDocument(attRef.getDocumentReference())
        .getAttachment(attRef.getName());
    if (att != null) {
      return newAttachmentData(att);
    } else {
      LOGGER.debug("can't queue inexistent attachment [{}]",
          defer(() -> modelUtils.serializeRef(attRef)));
      return null;
    }
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
    final StringBuilder docId = new StringBuilder();
    docId.append(modelUtils.serializeRef(References.extractRef(ref, EntityType.DOCUMENT).or(ref)));
    tryCast(ref, DocumentReference.class).ifPresent(
        docRef -> docId.append('.').append(tryGetLang(docRef).orElse("default")));
    tryCast(ref, AttachmentReference.class).ifPresent(
        attRef -> docId.append(".file.").append(attRef.getName()));
    return new DeleteData(docId.toString());
  }

  private Optional<String> tryGetLang(DocumentReference docRef) {
    return tryCast(docRef, QueueLangDocumentReference.class)
        .map(QueueLangDocumentReference::getLang)
        .filter(Optional::isPresent).map(Optional::get);
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
