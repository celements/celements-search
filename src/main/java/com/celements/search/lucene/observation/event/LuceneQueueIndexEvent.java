package com.celements.search.lucene.observation.event;

import com.celements.common.observation.converter.Remote;
import com.celements.search.lucene.observation.LuceneQueueEvent;

@Remote
public class LuceneQueueIndexEvent extends LuceneQueueEvent {

  private static final long serialVersionUID = -6212603792221276769L;

  public LuceneQueueIndexEvent() {
    super();
  }

  @Override
  public boolean isDelete() {
    return false;
  }

}
