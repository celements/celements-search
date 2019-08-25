package com.celements.search.lucene.observation;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.observation.save.SaveEventOperation;
import com.celements.search.lucene.index.DeleteData;
import com.celements.search.lucene.index.IndexData;
import com.celements.search.lucene.index.LuceneDocId;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.index.queue.IndexQueuePriorityManager;
import com.celements.search.lucene.index.queue.LuceneIndexingQueue;

public abstract class AbstractQueueListener<R extends EntityReference, S>
    extends AbstractRemoteEventListener<S, Object> {

  // TODO tests

  @Requirement
  private LuceneIndexingQueue indexingQueue;

  @Requirement
  private IndexQueuePriorityManager indexQueuePrioManager;

  @Override
  protected void onEventInternal(Event event, S source, Object data) {
    R ref = getReference(event, source);
    boolean isDelete = SaveEventOperation.from(event).isDelete();
    IndexData indexData;
    if (isDelete) {
      indexData = new DeleteData(new LuceneDocId(ref));
    } else {
      indexData = getIndexData(ref, source);
    }
    IndexQueuePriority priority = indexQueuePrioManager.getPriority()
        .orElse(IndexQueuePriority.DEFAULT);
    indexData.setPriority(priority);
    indexingQueue.add(indexData);
    LOGGER.info("queued{} at priority {}: {}", (isDelete ? " delete" : ""), priority, indexData.getId());
  }

  protected abstract R getReference(Event event, S source);

  protected abstract IndexData getIndexData(R ref, S source);

}
