package com.celements.search.lucene.observation;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.search.lucene.ILuceneIndexService;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.index.queue.QueueTask;

public abstract class AbstractQueueEventConverter<R extends EntityReference, S>
    extends AbstractLocalEventListener<S, Object> {

  @Requirement
  private ILuceneIndexService indexService;

  @Override
  protected void onEventInternal(Event event, S source, Object data) {
    R reference = getReference(event, source);
    QueueTask queueTask;
    if (isDeleteEvent(event)) {
      LOGGER.debug("notifying delete on [{}]", reference);
      queueTask = indexService.deleteTask(reference);
    } else {
      LOGGER.debug("notifying index on [{}]", reference);
      queueTask = indexService.indexTask(reference);
    }
    queueTask.priority(getPriority(source)).queue();
  }

  protected abstract boolean isDeleteEvent(Event event);

  protected abstract R getReference(Event event, S source);

  protected abstract IndexQueuePriority getPriority(S source);

}
