package com.celements.search.lucene.observation;

import static com.celements.common.MoreObjectsCel.*;

import java.util.List;

import org.xwiki.bridge.event.AbstractWikiEvent;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.model.reference.RefBuilder;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.google.common.collect.ImmutableList;

@Component(QueueWikiEventConverter.NAME)
public class QueueWikiEventConverter extends AbstractQueueEventConverter<WikiReference, Object> {

  public static final String NAME = "LuceneQueueWikiEventConverter";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return ImmutableList.of(
        new WikiCreatedEvent(),
        new WikiDeletedEvent());
  }

  @Override
  protected boolean isDeleteEvent(Event event) {
    return tryCast(event, WikiDeletedEvent.class).isPresent();
  }

  @Override
  protected WikiReference getReference(Event event, Object source) {
    AbstractWikiEvent wikiEvent = (AbstractWikiEvent) event;
    return RefBuilder.create()
        .with(EntityType.WIKI, wikiEvent.getWikiId())
        .build(WikiReference.class);
  }

  @Override
  protected IndexQueuePriority getPriority() {
    return IndexQueuePriority.LOW;
  }

}
