package com.celements.search.lucene.observation.event;

import java.util.Optional;

import org.xwiki.observation.event.AbstractFilterableEvent;

import com.celements.search.lucene.index.queue.IndexQueuePriority;

public abstract class LuceneQueueEvent extends AbstractFilterableEvent {

  private static final long serialVersionUID = 1562264153098581700L;

  private final IndexQueuePriority priority;

  public LuceneQueueEvent() {
    this(null);
  }

  public LuceneQueueEvent(IndexQueuePriority priority) {
    super();
    this.priority = priority;
  }

  public Optional<IndexQueuePriority> getPriority() {
    return Optional.ofNullable(priority);
  }

  public abstract boolean isDelete();

}
