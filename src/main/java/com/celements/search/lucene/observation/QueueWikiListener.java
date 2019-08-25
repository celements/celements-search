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
import com.celements.search.lucene.index.IndexData;
import com.celements.search.lucene.index.WikiData;

@Component(QueueWikiListener.NAME)
public class QueueWikiListener extends AbstractQueueListener<WikiReference, Object> {

  public static final String NAME = "celements.search.QueueWikiListener";

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

  @Override
  protected IndexData getIndexData(WikiReference wikiRef, Object source) {
    return new WikiData(wikiRef);
  }

}
