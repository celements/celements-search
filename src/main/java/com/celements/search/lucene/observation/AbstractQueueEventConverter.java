package com.celements.search.lucene.observation;

import java.util.stream.Stream;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.search.lucene.ILuceneIndexService;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.index.queue.QueueTask;

public abstract class AbstractQueueEventConverter<S>
    extends AbstractLocalEventListener<S, Object> {

  @Requirement
  private ILuceneIndexService indexService;

  @Override
  protected void onEventInternal(Event event, S source, Object data) {
    getReferences(event, source).forEach(reference -> {
      QueueTask queueTask = isDeleteEvent(event)
          ? indexService.deleteTask(reference)
          : indexService.indexTask(reference);
      queueTask.priority(getPriority(reference))
          .queue();
      LOGGER.debug("queued: [{}] ", queueTask);
    });
  }

  protected abstract boolean isDeleteEvent(Event event);

  protected abstract Stream<EntityReference> getReferences(Event event, S source);

  protected abstract IndexQueuePriority getPriority(EntityReference reference);

}
