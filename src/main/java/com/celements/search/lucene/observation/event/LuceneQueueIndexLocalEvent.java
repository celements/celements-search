package com.celements.search.lucene.observation.event;

import com.celements.common.observation.converter.Local;
import com.celements.search.lucene.index.queue.IndexQueuePriority;

@Local
public class LuceneQueueIndexLocalEvent extends LuceneQueueEvent {

  private static final long serialVersionUID = -8716529696014788955L;

  public LuceneQueueIndexLocalEvent() {
    super();
  }

  public LuceneQueueIndexLocalEvent(IndexQueuePriority priority) {
    super(priority);
  }

  @Override
  public boolean isDelete() {
    return false;
  }

}
