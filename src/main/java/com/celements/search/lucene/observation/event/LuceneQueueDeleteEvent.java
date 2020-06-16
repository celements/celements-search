package com.celements.search.lucene.observation.event;

import com.celements.common.observation.converter.Remote;
import com.celements.search.lucene.observation.LuceneQueueEvent;

@Remote
public class LuceneQueueDeleteEvent extends LuceneQueueEvent {

  private static final long serialVersionUID = 2430479462843286839L;

  public LuceneQueueDeleteEvent() {
    super();
  }

  @Override
  public boolean isDelete() {
    return true;
  }

}
