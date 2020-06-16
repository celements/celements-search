package com.celements.search.lucene.observation;

import java.io.Serializable;
import java.util.Objects;

import org.xwiki.observation.event.AbstractFilterableEvent;

import com.celements.common.observation.converter.Remote;
import com.celements.search.lucene.index.queue.IndexQueuePriority;

@Remote
public abstract class LuceneQueueEvent extends AbstractFilterableEvent {

  private static final long serialVersionUID = 1562264153098581700L;

  public LuceneQueueEvent() {
    super();
  }

  public abstract boolean isDelete();

  public static class Data implements Serializable {

    private static final long serialVersionUID = 3836590510200869274L;

    public static final Data DEFAULT = new Data(IndexQueuePriority.DEFAULT, false);

    public final IndexQueuePriority priority;
    public final boolean disableEventNotification;

    public Data(IndexQueuePriority priority, boolean disableEventNotification) {
      this.priority = priority;
      this.disableEventNotification = disableEventNotification;
    }

    @Override
    public int hashCode() {
      return Objects.hash(priority, disableEventNotification);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Data) {
        Data other = (Data) obj;
        return (this.priority == other.priority)
            && (this.disableEventNotification == other.disableEventNotification);
      }
      return false;
    }

    @Override
    public String toString() {
      return "LuceneQueueEvent.Data [priority=" + priority + ", disableEventNotification="
          + disableEventNotification + "]";
    }

  }

}
