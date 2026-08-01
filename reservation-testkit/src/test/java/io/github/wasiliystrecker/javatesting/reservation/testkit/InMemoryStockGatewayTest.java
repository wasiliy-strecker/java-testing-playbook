package io.github.wasiliystrecker.javatesting.reservation.testkit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class InMemoryStockGatewayTest {

  @Test
  void atomicallyLimitsConcurrentDecreasesToAvailableStock()
      throws InterruptedException, ExecutionException {
    Sku sku = new Sku("SKU-CONCURRENT");
    InMemoryStockGateway stock = new InMemoryStockGateway().setAvailable(sku, 37);
    CountDownLatch start = new CountDownLatch(1);

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<Boolean>> attempts = new ArrayList<>();
      for (int index = 0; index < 100; index++) {
        attempts.add(
            executor.submit(
                () -> {
                  start.await();
                  return stock.tryDecrease(sku, new Quantity(1));
                }));
      }

      start.countDown();
      long accepted = 0;
      for (Future<Boolean> attempt : attempts) {
        if (attempt.get()) {
          accepted++;
        }
      }

      assertThat(accepted).isEqualTo(37);
      assertThat(stock.available(sku)).isZero();
    }
  }
}
