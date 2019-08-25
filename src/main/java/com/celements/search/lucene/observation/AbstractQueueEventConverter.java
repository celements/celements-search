package com.celements.search.lucene.observation;

import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.search.lucene.observation.event.LuceneQueueDeleteLocalEvent;
import com.celements.search.lucene.observation.event.LuceneQueueIndexLocalEvent;

public abstract class AbstractQueueEventConverter<R extends EntityReference, S>
    extends AbstractRemoteEventListener<S, Object> {

  @Override
  protected void onEventInternal(Event event, S source, Object data) {
    Event notifyEvent = event.getClass().getSimpleName().endsWith("DeletedEvent")
        ? new LuceneQueueDeleteLocalEvent()
        : new LuceneQueueIndexLocalEvent();
    R ref = getReference(event, source);
    LOGGER.debug("notifying [{}] on [{}]", notifyEvent, ref);
    getObservationManager().notify(notifyEvent, ref, null);
  }

  protected abstract R getReference(Event event, S source);

}
