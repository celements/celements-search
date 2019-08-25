package com.celements.search.lucene.observation;

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;

import com.celements.search.lucene.index.DocumentData;
import com.celements.search.lucene.index.IndexData;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(QueueDocumentListener.NAME)
public class QueueDocumentListener extends AbstractQueueListener<DocumentReference, XWikiDocument> {

  public static final String NAME = "celements.search.QueueDocumentListener";

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

  @Override
  protected IndexData getIndexData(DocumentReference docRef, XWikiDocument doc) {
    return new DocumentData(doc);
  }

}
