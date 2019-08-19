package com.celements.search.lucene.index.queue;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.celements.common.MoreObjectsCel;
import com.celements.search.lucene.index.LuceneDocId;
import com.google.common.collect.ComparisonChain;

/**
 * This class represents an element of a lucene indexing priority queue. The order is primarly
 * defined by it's {@link IndexQueuePriority}. On equal priority, a sequence number will emulate
 * FIFO behaviour.
 */
class IndexQueueElement implements Comparable<IndexQueueElement> {

  private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong();

  private final LuceneDocId id;
  private final IndexQueuePriority priority;
  private final long sequenceNb;

  IndexQueueElement(LuceneDocId id, IndexQueuePriority priority) {
    this.id = checkNotNull(id);
    this.priority = firstNonNull(priority, IndexQueuePriority.DEFAULT);
    this.sequenceNb = SEQUENCE_COUNTER.incrementAndGet();
  }

  public LuceneDocId getId() {
    return id;
  }

  public IndexQueuePriority getPriority() {
    return priority;
  }

  @Override
  public int compareTo(IndexQueueElement other) {
    ComparisonChain cmp = ComparisonChain.start();
    cmp = cmp.compare(this.priority, other.priority);
    cmp = cmp.compare(this.sequenceNb, other.sequenceNb);
    return cmp.result();
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    return MoreObjectsCel.tryCast(obj, IndexQueueElement.class)
        .map(other -> Objects.equals(this.id, other.id))
        .orElse(false);
  }

  @Override
  public String toString() {
    return "IndexQueueElement [id=" + id + ", priority=" + priority + ", sequenceNb=" + sequenceNb
        + "]";
  }

}
