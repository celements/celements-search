package com.celements.search.lucene.observation.event;

import com.celements.common.observation.converter.Remote;
import com.celements.search.lucene.index.queue.IndexQueuePriority;

@Remote
public class LuceneQueueDeleteEvent extends LuceneQueueEvent {

  private static final long serialVersionUID = 2430479462843286839L;

  public LuceneQueueDeleteEvent() {
    super();
  }

  public LuceneQueueDeleteEvent(IndexQueuePriority priority) {
    super(priority);
  }

  @Override
  public boolean isDelete() {
    return true;
  }

}
