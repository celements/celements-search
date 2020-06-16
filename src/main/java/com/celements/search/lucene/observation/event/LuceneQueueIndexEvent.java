package com.celements.search.lucene.observation.event;

import com.celements.common.observation.converter.Remote;

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
