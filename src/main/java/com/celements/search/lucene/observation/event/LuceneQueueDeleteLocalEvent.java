package com.celements.search.lucene.observation.event;

import com.celements.common.observation.converter.Local;
import com.celements.search.lucene.index.queue.IndexQueuePriority;

@Local
public class LuceneQueueDeleteLocalEvent extends LuceneQueueEvent {

  private static final long serialVersionUID = -1558064658025351665L;

  public LuceneQueueDeleteLocalEvent() {
    super();
  }

  public LuceneQueueDeleteLocalEvent(IndexQueuePriority priority) {
    super(priority);
  }

  @Override
  public boolean isDelete() {
    return true;
  }

}
