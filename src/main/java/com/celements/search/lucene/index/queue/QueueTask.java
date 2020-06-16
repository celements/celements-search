package com.celements.search.lucene.index.queue;

import static java.util.Objects.*;

import java.util.Optional;

import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.ObservationManager;

import com.celements.search.lucene.observation.event.LuceneQueueEvent;
import com.xpn.xwiki.web.Utils;

public class QueueTask {

  private final EntityReference ref;
  private final LuceneQueueEvent event;
  private IndexQueuePriority priority;
  private Boolean withoutNotifications;

  public QueueTask(EntityReference ref, LuceneQueueEvent event) {
    System.err.println(ref);
    this.ref = requireNonNull(ref);
    this.event = requireNonNull(event);
  }

  public EntityReference getReference() {
    return ref;
  }

  public QueueTask priority(IndexQueuePriority priority) {
    this.priority = priority;
    return this;
  }

  public IndexQueuePriority getPriority() {
    return LuceneQueueExecutionSettings.getPriority()
        .orElseGet(() -> Optional.ofNullable(priority)
        .orElse(LuceneQueueEvent.Data.DEFAULT.priority));
  }

  public QueueTask withoutNotifications() {
    this.withoutNotifications = true;
    return this;
  }

  public boolean getDisableEventNotification() {
    return LuceneQueueExecutionSettings.getDisableEventNotification()
        .orElseGet(() -> Optional.ofNullable(withoutNotifications)
        .orElse(LuceneQueueEvent.Data.DEFAULT.disableEventNotification));
  }

  public void queue() {
    getObservationManager().notify(event, ref,
        new LuceneQueueEvent.Data(getPriority(), getDisableEventNotification()));
  }

  private static ObservationManager getObservationManager() {
    return Utils.getComponent(ObservationManager.class);
  }

  @Override
  public String toString() {
    return "QueueTask [ref=" + ref + ", event=" + event + ", priority=" + getPriority()
        + ", disableEventNotification=" + getDisableEventNotification() + "]";
  }

}
