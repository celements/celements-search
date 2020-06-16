package com.celements.search.lucene.observation;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.search.lucene.ILuceneIndexService;
import com.celements.search.lucene.index.queue.IndexQueuePriority;

public abstract class AbstractQueueEventConverter<R extends EntityReference, S>
    extends AbstractLocalEventListener<S, Object> {

  @Requirement
  private ILuceneIndexService indexService;

  @Override
  protected void onEventInternal(Event event, S source, Object data) {
    R reference = getReference(event, source);
    if (isDeleteEvent(event)) {
      LOGGER.debug("notifying delete on [{}]", reference);
      indexService.queueDelete(reference, getPriority());
    } else {
      LOGGER.debug("notifying index on [{}]", reference);
      indexService.queue(reference, getPriority());
    }
  }

  protected abstract boolean isDeleteEvent(Event event);

  protected abstract R getReference(Event event, S source);

  protected IndexQueuePriority getPriority() {
    return null;
  }

}
