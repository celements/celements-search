package com.celements.search.lucene.observation;

import static com.celements.common.MoreObjectsCel.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.observation.event.Event;

import com.celements.model.reference.RefBuilder;
import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.AbstractAttachmentEvent;
import com.xpn.xwiki.internal.event.AttachmentAddedEvent;
import com.xpn.xwiki.internal.event.AttachmentDeletedEvent;
import com.xpn.xwiki.internal.event.AttachmentUpdatedEvent;

@Component(QueueAttachmentEventConverter.NAME)
public class QueueAttachmentEventConverter
    extends AbstractQueueEventConverter<AttachmentReference, XWikiDocument> {

  public static final String NAME = "LuceneQueueAttachmentEventConverter";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return ImmutableList.of(
        new AttachmentAddedEvent(),
        new AttachmentUpdatedEvent(),
        new AttachmentDeletedEvent());
  }

  @Override
  protected boolean isDeleteEvent(Event event) {
    return tryCast(event, AttachmentDeletedEvent.class).isPresent();
  }

  @Override
  protected AttachmentReference getReference(Event event, XWikiDocument doc) {
    AbstractAttachmentEvent attachEvent = (AbstractAttachmentEvent) event;
    return RefBuilder.from(doc.getDocumentReference())
        .with(EntityType.ATTACHMENT, attachEvent.getName())
        .build(AttachmentReference.class);
  }

  @Override
  protected IndexQueuePriority getPriority(XWikiDocument doc) {
    return IndexQueuePriority.LOW;
  }

}
