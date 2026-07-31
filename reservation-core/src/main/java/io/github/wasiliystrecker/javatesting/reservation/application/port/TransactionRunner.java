package io.github.wasiliystrecker.javatesting.reservation.application.port;

public interface TransactionRunner {

  <T> T execute(TransactionalOperation<T> operation);

  @FunctionalInterface
  interface TransactionalOperation<T> {

    T execute();
  }
}
