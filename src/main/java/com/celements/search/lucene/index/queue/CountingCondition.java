package com.celements.search.lucene.index.queue;

import static com.google.common.base.Preconditions.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Function;

/**
 * like {@link Condition} but provides feedback if any waiters exist {@link #hasWaiters()} or any
 * have been signaled {@link #signalWaiterIfAny()}.
 * Similar to {@link ReentrantLock#hasWaiters(Condition)}, but this method doesn't guarantee the
 * required reliability since it's "primarily for use in monitoring of the system state" (see
 * javadoc).
 */
class CountingCondition {

  private final Condition condition;
  private final int awaitSeconds;
  private final AtomicLong count;

  public CountingCondition(Condition condition, int awaitSeconds) {
    this.condition = checkNotNull(condition);
    this.awaitSeconds = awaitSeconds;
    checkArgument(awaitSeconds > 0);
    this.count = new AtomicLong();
  }

  public void await() throws InterruptedException {
    count.incrementAndGet();
    condition.await(awaitSeconds, TimeUnit.SECONDS);
    count.decrementAndGet();
  }

  public boolean hasWaiters() {
    return count.get() > 0;
  }

  public boolean signalWaiterIfAny() {
    boolean signaled = false;
    if (hasWaiters()) {
      condition.signal();
      signaled = true;
    }
    return signaled;
  }

  static class CreateFunction<T> implements Function<T, CountingCondition> {

    private final ReentrantLock lock;
    private final int awaitSeconds;

    public CreateFunction(ReentrantLock lock, int awaitSeconds) {
      this.lock = lock;
      this.awaitSeconds = awaitSeconds;
    }

    @Override
    public CountingCondition apply(T anyObj) {
      return new CountingCondition(lock.newCondition(), awaitSeconds);
    }

  }
}
