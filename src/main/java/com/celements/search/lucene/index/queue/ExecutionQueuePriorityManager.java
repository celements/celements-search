package com.celements.search.lucene.index.queue;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.google.common.base.Optional;

@Component
public class ExecutionQueuePriorityManager implements IndexQueuePriorityManager {

  private static final String EXEC_CONTEXT_KEY = "lucene.index.queue.priority";

  @Requirement
  private Execution exec;

  @Override
  public Optional<IndexQueuePriority> getPriority() {
    IndexQueuePriority priority = null;
    Object property = getExecContext().getProperty(EXEC_CONTEXT_KEY);
    if (property instanceof IndexQueuePriority) {
      priority = (IndexQueuePriority) property;
    }
    return Optional.fromNullable(priority);
  }

  @Override
  public void putPriority(IndexQueuePriority priority) {
    getExecContext().setProperty(EXEC_CONTEXT_KEY, priority);
  }

  private ExecutionContext getExecContext() {
    return exec.getContext();
  }

}
