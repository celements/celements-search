package com.celements.search.lucene.observation;

import java.util.Arrays;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.observation.event.Event;

import com.celements.model.reference.RefBuilder;
import com.celements.search.lucene.index.AttachmentData;
import com.celements.search.lucene.index.IndexData;
import com.celements.search.lucene.index.queue.LuceneIndexingQueue;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.AbstractAttachmentEvent;
import com.xpn.xwiki.internal.event.AttachmentAddedEvent;
import com.xpn.xwiki.internal.event.AttachmentDeletedEvent;
import com.xpn.xwiki.internal.event.AttachmentUpdatedEvent;

@Component(QueueAttachmentListener.NAME)
public class QueueAttachmentListener extends AbstractQueueListener<AttachmentReference, XWikiDocument> {

  public static final String NAME = "celements.search.QueueAttachmentListener";

  @Requirement
  private LuceneIndexingQueue indexingQueue;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(
        new AttachmentAddedEvent(),
        new AttachmentUpdatedEvent(),
        new AttachmentDeletedEvent());
  }

  @Override
  protected AttachmentReference getReference(Event event, XWikiDocument doc) {
    AbstractAttachmentEvent attachEvent = (AbstractAttachmentEvent) event;
    return RefBuilder.from(doc.getDocumentReference())
        .with(EntityType.ATTACHMENT, attachEvent.getName())
        .build(AttachmentReference.class);
  }

  @Override
  protected IndexData getIndexData(AttachmentReference attRef, XWikiDocument doc) {
    return new AttachmentData(doc, attRef.getName());
  }

}
