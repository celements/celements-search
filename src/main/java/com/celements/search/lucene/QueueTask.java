package com.celements.search.lucene;

import static com.celements.common.MoreObjectsCel.*;
import static java.util.Objects.*;

import java.util.Optional;

import org.xwiki.context.Execution;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.ObservationManager;

import com.celements.search.lucene.index.queue.IndexQueuePriority;
import com.celements.search.lucene.observation.event.LuceneQueueEvent;
import com.xpn.xwiki.web.Utils;

public class QueueTask {

  public static final String EXEC_QUEUE_PRIORITY = "lucene.index.queue.priority";
  public static final String EXEC_DISABLE_EVENT_NOTIFICATION = "lucene.index.disableEventNotification";

  private final EntityReference ref;
  private final LuceneQueueEvent event;
  private IndexQueuePriority priority;
  private Boolean withoutNotifications;

  QueueTask(EntityReference ref, LuceneQueueEvent event) {
    this.ref = requireNonNull(ref);
    this.event = requireNonNull(event);
  }

  public QueueTask priority(IndexQueuePriority priority) {
    this.priority = priority;
    return this;
  }

  public IndexQueuePriority getPriority() {
    return Optional.ofNullable(priority)
        .orElseGet(() -> getExecutionParam(EXEC_QUEUE_PRIORITY, IndexQueuePriority.class)
        .orElse(LuceneQueueEvent.Data.DEFAULT.priority));
  }

  public QueueTask withoutNotifications() {
    this.withoutNotifications = true;
    return this;
  }

  public boolean getDisableEventNotification() {
    return Optional.ofNullable(withoutNotifications)
        .orElseGet(() -> getExecutionParam(EXEC_DISABLE_EVENT_NOTIFICATION, Boolean.class)
        .orElse(LuceneQueueEvent.Data.DEFAULT.disableEventNotification));
  }

  public void queue() {
    getObservationManager().notify(event, ref,
        new LuceneQueueEvent.Data(getPriority(), getDisableEventNotification()));
  }

  /**
   * loaded lazily due to cyclic dependency
   */
  private ObservationManager getObservationManager() {
    return Utils.getComponent(ObservationManager.class);
  }

  private <T> Optional<T> getExecutionParam(String key, Class<T> type) {
    return tryCast(Utils.getComponent(Execution.class).getContext().getProperty(key), type);
  }

}
