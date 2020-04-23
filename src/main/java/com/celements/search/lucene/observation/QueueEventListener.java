package com.celements.search.lucene.observation;

import static com.celements.logging.LogUtils.*;

import java.util.Arrays;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractRemoteEventListener;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

@Component(QueueEventListener.NAME)
public class QueueEventListener extends AbstractRemoteEventListener<EntityReference, Void> {

  public static final String NAME = "celements.search.QueueEventListener";

  @Requirement
  private ModelUtils modelUtils;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.asList(new LuceneQueueEvent());
  }

  @Override
  protected void onEventInternal(Event event, EntityReference ref, Void data) {
    try {
      if (isLucenePluginAvailable()) {
        if (ref instanceof DocumentReference) {
          queueDocumentWithAttachments((DocumentReference) ref);
        } else if (ref instanceof AttachmentReference) {
          queueAttachment((AttachmentReference) ref);
        } else {
          LOGGER.warn("unable to queue ref [{}]", defer(() -> modelUtils.serializeRef(ref)));
        }
      } else {
        LOGGER.warn("LucenePlugin not available, first request?");
      }
    } catch (DocumentNotExistsException dne) {
      LOGGER.debug("can't queue inexistend document [{}]", modelUtils.serializeRef(ref), dne);
    }
  }

  private void queueDocumentWithAttachments(DocumentReference docRef)
      throws DocumentNotExistsException {
    LOGGER.debug("adding to queue [{}]", defer(() -> modelUtils.serializeRef(docRef)));
    XWikiDocument doc = modelAccess.getDocument(docRef);
    getLucenePlugin().queueDocument(doc, context.getXWikiContext());
    getLucenePlugin().queueAttachment(doc, context.getXWikiContext());
  }

  private void queueAttachment(AttachmentReference attRef) throws DocumentNotExistsException {
    LOGGER.debug("adding to queue [{}]", defer(() -> modelUtils.serializeRef(attRef)));
    XWikiDocument doc = modelAccess.getDocument(attRef.getDocumentReference());
    getLucenePlugin().queueAttachment(doc, doc.getAttachment(attRef.getName()),
        context.getXWikiContext());
  }

  private boolean isLucenePluginAvailable() {
    try {
      return (getLucenePlugin() != null);
    } catch (NullPointerException npe) {
      return false;
    }
  }

  private LucenePlugin getLucenePlugin() {
    return (LucenePlugin) context.getXWikiContext().getWiki().getPlugin(
        "lucene", context.getXWikiContext());
  }

}
