package io.github.wasiliystrecker.javatesting.reservation.testkit;

import io.github.wasiliystrecker.javatesting.reservation.application.port.TransactionRunner;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/** Executes work inline while recording transaction-boundary invocations. */
public final class RecordingTransactionRunner implements TransactionRunner {

  private final AtomicInteger invocationCount = new AtomicInteger();

  @Override
  public <T> T execute(TransactionalOperation<T> operation) {
    Objects.requireNonNull(operation, "operation must not be null");
    invocationCount.incrementAndGet();
    return operation.execute();
  }

  public int invocationCount() {
    return invocationCount.get();
  }
}
