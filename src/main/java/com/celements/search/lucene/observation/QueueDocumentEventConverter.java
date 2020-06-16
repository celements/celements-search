package com.celements.search.lucene.observation;

import static com.celements.common.MoreObjectsCel.*;

import java.util.List;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;

import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(QueueDocumentEventConverter.NAME)
public class QueueDocumentEventConverter
    extends AbstractQueueEventConverter<DocumentReference, XWikiDocument> {

  public static final String NAME = "LuceneQueueDocumentEventConverter";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return ImmutableList.of(
        new DocumentCreatedEvent(),
        new DocumentUpdatedEvent(),
        new DocumentDeletedEvent());
  }

  @Override
  protected boolean isDeleteEvent(Event event) {
    return tryCast(event, DocumentDeletedEvent.class).isPresent();
  }

  @Override
  protected DocumentReference getReference(Event event, XWikiDocument doc) {
    return doc.getDocumentReference();
  }

}
