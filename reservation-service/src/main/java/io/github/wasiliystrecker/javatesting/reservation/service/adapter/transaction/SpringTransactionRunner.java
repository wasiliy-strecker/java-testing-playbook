package io.github.wasiliystrecker.javatesting.reservation.service.adapter.transaction;

import io.github.wasiliystrecker.javatesting.reservation.application.port.TransactionRunner;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public final class SpringTransactionRunner implements TransactionRunner {

  private final TransactionTemplate transactions;

  public SpringTransactionRunner(PlatformTransactionManager transactionManager) {
    transactions = new TransactionTemplate(transactionManager);
  }

  @Override
  public <T> T execute(TransactionalOperation<T> operation) {
    Objects.requireNonNull(operation, "operation must not be null");
    return transactions.execute(status -> operation.execute());
  }
}
