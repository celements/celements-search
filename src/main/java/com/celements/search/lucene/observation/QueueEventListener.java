package com.celements.search.lucene.observation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.celements.search.lucene.index.AttachmentData;
import com.celements.search.lucene.index.DeleteData;
import com.celements.search.lucene.index.DocumentData;
import com.celements.search.lucene.index.IndexData;
import com.celements.search.lucene.index.LuceneDocId;
import com.celements.search.lucene.index.WikiData;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.index.queue.IndexQueuePriorityManager;
import com.celements.search.lucene.index.queue.LuceneIndexingQueue;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteEvent;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteLocalEvent;
import com.celements.search.lucene.observation.event.LuceneQueueEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexLocalEvent;
import com.xpn.xwiki.doc.XWikiAttachment;

@Component(QueueEventListener.NAME)
public class QueueEventListener extends AbstractRemoteEventListener<LuceneDocId, Void> {

  public static final String NAME = "LuceneQueueEventListener";

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private LuceneIndexingQueue indexingQueue;

  @Requirement
  private IndexQueuePriorityManager indexQueuePrioManager;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.asList(
        new LuceneQueueIndexEvent(),
        new LuceneQueueDeleteEvent(),
        new LuceneQueueIndexLocalEvent(),
        new LuceneQueueDeleteLocalEvent());
  }

  @Override
  protected void onEventInternal(Event event, LuceneDocId docId, Void data) {
    LuceneQueueEvent queueEvent = (LuceneQueueEvent) event;
    try {
      buildIndexData(queueEvent, docId).ifPresent(indexData -> {
        IndexQueuePriority priority = queueEvent.getPriority()
            .orElse(indexQueuePrioManager.getPriority()
                .orElse(IndexQueuePriority.DEFAULT));
        indexData.setPriority(priority);
        indexingQueue.add(indexData);
        LOGGER.info("queued{} at priority {}: {}", (queueEvent.isDelete() ? " delete" : ""),
            priority, indexData.getId());
      });
    } catch (DocumentNotExistsException | AttachmentNotExistsException exc) {
      LOGGER.warn("failed queing [{}]: {}", docId, exc.getMessage(), exc);
    }
  }

  private Optional<IndexData> buildIndexData(LuceneQueueEvent event, LuceneDocId docId)
      throws DocumentNotExistsException, AttachmentNotExistsException {
    IndexData data = null;
    if (event.isDelete()) {
      data = new DeleteData(docId);
    } else if (docId.getRef() instanceof WikiReference) {
      data = new WikiData((WikiReference) docId.getRef());
    } else if (docId.getRef() instanceof DocumentReference) {
      data = new DocumentData(modelAccess.getDocument(
          (DocumentReference) docId.getRef(), docId.getLang()));
    } else if (docId.getRef() instanceof AttachmentReference) {
      AttachmentReference attRef = (AttachmentReference) docId.getRef();
      XWikiAttachment attach = modelAccess.getAttachmentNameEqual(
          modelAccess.getDocument(attRef.getDocumentReference()), attRef.getName());
      data = new AttachmentData(attach);
    } else {
      LOGGER.warn("unable to queue [{}]", docId);
    }
    return Optional.ofNullable(data);
  }

}
