package com.celements.search.lucene.observation;

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.event.AbstractWikiEvent;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.model.reference.RefBuilder;

@Component(QueueWikiEventConverter.NAME)
public class QueueWikiEventConverter extends AbstractQueueEventConverter<WikiReference, Object> {

  public static final String NAME = "LuceneQueueWikiEventConverter";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(
        new WikiCreatedEvent(),
        new WikiDeletedEvent());
  }

  @Override
  protected WikiReference getReference(Event event, Object source) {
    AbstractWikiEvent wikiEvent = (AbstractWikiEvent) event;
    return RefBuilder.create()
        .with(EntityType.WIKI, wikiEvent.getWikiId())
        .build(WikiReference.class);
  }

}
