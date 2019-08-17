package com.celements.search.lucene.index.queue;

import static com.google.common.base.Preconditions.*;

import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;

class PriorityCondition {

  private final Supplier<Condition> conditionSupplier;
  private final TreeMap<IndexQueuePriority, Condition> priorityMap;

  public PriorityCondition(Supplier<Condition> conditionSupplier) {
    this.conditionSupplier = checkNotNull(conditionSupplier);
    priorityMap = new TreeMap<>();
  }

  public void await(IndexQueuePriority priority, int seconds) throws InterruptedException {
    priorityMap.computeIfAbsent(priority, k -> conditionSupplier.get())
        .await(seconds, TimeUnit.SECONDS);
  }

  public void signal() {
    Optional.ofNullable(priorityMap.pollFirstEntry())
        .map(entry -> entry.getValue())
        .ifPresent(cond -> cond.signal());
  }

}
