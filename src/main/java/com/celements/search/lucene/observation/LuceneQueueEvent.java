package com.celements.search.lucene.observation;

import java.io.Serializable;
import java.util.Objects;

import org.xwiki.observation.event.AbstractFilterableEvent;

import com.celements.common.observation.converter.Remote;
import com.celements.search.lucene.index.queue.IndexQueuePriority;

@Remote
public class LuceneQueueEvent extends AbstractFilterableEvent {

  private static final long serialVersionUID = -6212603792221276769L;

  public LuceneQueueEvent() {
    super();
  }

  public static class Data implements Serializable {

    private static final long serialVersionUID = -9175347513638637434L;

    public static final Data DEFAULT = new Data(IndexQueuePriority.DEFAULT, false);

    public final IndexQueuePriority priority;
    public final boolean disableEventNotification;

    public Data(IndexQueuePriority priority, boolean disableEventNotification) {
      this.priority = priority;
      this.disableEventNotification = disableEventNotification;
    }

    @Override
    public int hashCode() {
      return Objects.hash(disableEventNotification, priority);
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
