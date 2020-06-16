package com.celements.search.lucene.observation;

import static com.celements.common.MoreObjectsCel.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(QueueDocumentEventConverter.NAME)
public class QueueDocumentEventConverter extends AbstractQueueEventConverter<XWikiDocument> {

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
  protected Stream<EntityReference> getReferences(Event event, XWikiDocument doc) {
    return Stream.concat(Stream.of(doc.getDocumentReference()),
        doc.getAttachmentList().stream()
            .filter(Objects::nonNull)
            .map(att -> new AttachmentReference(att.getFilename(), doc.getDocumentReference())));
  }

  @Override
  protected IndexQueuePriority getPriority(EntityReference ref) {
    if (ref.getType() == EntityType.ATTACHMENT) {
      return IndexQueuePriority.LOW;
    } else if (context.getCurrentDocRef().toJavaUtil().map(docRef -> docRef.equals(ref))
        .orElse(false)) {
      return IndexQueuePriority.HIGHEST;
    }
    return null;
  }

}
