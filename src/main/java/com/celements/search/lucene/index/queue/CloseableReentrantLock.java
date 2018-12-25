package com.celements.search.lucene.index.queue;

import java.util.concurrent.locks.ReentrantLock;

public class CloseableReentrantLock extends ReentrantLock {

  private static final long serialVersionUID = -3400341018166675504L;

  private final CloseableLock closeable = new CloseableLock();

  public CloseableLock open() {
    this.lock();
    return closeable;
  }

  public class CloseableLock implements AutoCloseable {

    @Override
    public void close() {
      CloseableReentrantLock.this.unlock();
    }

  }
}
