package com.celements.search.lucene.observation;

import org.xwiki.observation.event.AbstractFilterableEvent;

import com.celements.common.observation.converter.Remote;

@Remote
public class LuceneQueueEvent extends AbstractFilterableEvent {

  private static final long serialVersionUID = -6212603792221276769L;

  public LuceneQueueEvent() {
    super();
  }

}
