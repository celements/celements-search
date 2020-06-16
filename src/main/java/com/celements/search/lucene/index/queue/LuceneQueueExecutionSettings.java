package com.celements.search.lucene.index.queue;

import static com.celements.common.MoreObjectsCel.*;

import java.util.Optional;
import java.util.function.Supplier;

import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.celements.common.lambda.LambdaExceptionUtil.ThrowingSupplier;
import com.xpn.xwiki.web.Utils;

public class LuceneQueueExecutionSettings {

  static final String EXEC_QUEUE_PRIORITY = "lucene.index.queue.priority";

  public static Optional<IndexQueuePriority> getPriority() {
    return getExecutionParam(EXEC_QUEUE_PRIORITY, IndexQueuePriority.class);
  }

  public static <T> T executeWithPriority(IndexQueuePriority priority, Supplier<T> supplier) {
    return executeWithPriorityThrows(priority, supplier::get);
  }

  public static <T, E extends Exception> T executeWithPriorityThrows(IndexQueuePriority priority,
      ThrowingSupplier<T, E> supplier) throws E {
    Object before = getExecutionContext().getProperty(EXEC_QUEUE_PRIORITY);
    try {
      getExecutionContext().setProperty(EXEC_QUEUE_PRIORITY, priority);
      return supplier.get();
    } finally {
      getExecutionContext().setProperty(EXEC_QUEUE_PRIORITY, before);
    }
  }

  static final String EXEC_DISABLE_EVENT_NOTIFICATION = "lucene.index.disableEventNotification";

  public static Optional<Boolean> getDisableEventNotification() {
    return getExecutionParam(EXEC_DISABLE_EVENT_NOTIFICATION, Boolean.class);
  }

  public static <T> T executeWithoutNotifications(Supplier<T> supplier) {
    return executeWithoutNotificationsThrows(supplier::get);
  }

  public static <T> T executeWithoutNotificationsThrows(Supplier<T> supplier) {
    Object before = getExecutionContext().getProperty(EXEC_DISABLE_EVENT_NOTIFICATION);
    try {
      getExecutionContext().setProperty(EXEC_DISABLE_EVENT_NOTIFICATION, Boolean.TRUE);
      return supplier.get();
    } finally {
      getExecutionContext().setProperty(EXEC_DISABLE_EVENT_NOTIFICATION, before);
    }
  }

  private static <T> Optional<T> getExecutionParam(String key, Class<T> type) {
    return tryCast(getExecutionContext().getProperty(key), type);
  }

  private static ExecutionContext getExecutionContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

  private LuceneQueueExecutionSettings() {}

}
