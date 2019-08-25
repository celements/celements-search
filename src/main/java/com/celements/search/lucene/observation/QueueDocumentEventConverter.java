package com.celements.search.lucene.observation;

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;

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
    return Arrays.<Event>asList(
        new DocumentCreatedEvent(),
        new DocumentUpdatedEvent(),
        new DocumentDeletedEvent());
  }

  @Override
  protected DocumentReference getReference(Event event, XWikiDocument doc) {
    return doc.getDocumentReference();
  }

}
