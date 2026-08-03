package io.github.wasiliystrecker.javatesting.reservation.service;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.wasiliystrecker.javatesting.reservation.application.CatalogProductStatus;
import io.github.wasiliystrecker.javatesting.reservation.application.InsufficientStockException;
import io.github.wasiliystrecker.javatesting.reservation.application.ReservationAlreadyExistsException;
import io.github.wasiliystrecker.javatesting.reservation.application.ReservationService;
import io.github.wasiliystrecker.javatesting.reservation.application.ReserveStockCommand;
import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.application.port.StockGateway;
import io.github.wasiliystrecker.javatesting.reservation.application.port.TransactionRunner;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import io.github.wasiliystrecker.javatesting.reservation.testkit.DeterministicReservationIdGenerator;
import io.github.wasiliystrecker.javatesting.reservation.testkit.ReservationBuilder;
import io.github.wasiliystrecker.javatesting.reservation.testkit.StubCatalogGateway;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest(classes = ReservationServiceApplication.class)
@Import(TestPostgreSqlConfiguration.class)
class PostgreSqlTransactionSafetyIT {

  private static final int CONCURRENT_ATTEMPTS = 100;
  private static final int INITIAL_STOCK = 37;
  private static final long COORDINATION_TIMEOUT_SECONDS = 30;

  private final PostgreSqlTestFixture database;
  private final ReservationRepository reservations;
  private final StockGateway stock;
  private final TransactionRunner transactions;

  @Autowired
  PostgreSqlTransactionSafetyIT(
      JdbcClient jdbc,
      ReservationRepository reservations,
      StockGateway stock,
      TransactionRunner transactions) {
    database = new PostgreSqlTestFixture(jdbc);
    this.reservations = reservations;
    this.stock = stock;
    this.transactions = transactions;
  }

  @BeforeEach
  void resetDatabase() {
    database.reset();
  }

  @Test
  void rollsBackStockDecreaseWhenReservationInsertConflicts() {
    Sku sku = new Sku("SKU-ROLLBACK-INSERT");
    Reservation original =
        ReservationBuilder.aReservation().withSku(sku.value()).withQuantity(1).confirmed();
    Reservation duplicate =
        ReservationBuilder.aReservation()
            .withId(original.id().value())
            .withSku(sku.value())
            .withQuantity(2)
            .confirmed();
    database.insertStock(sku, 5);
    reservations.insert(original);

    assertThatThrownBy(
            () ->
                transactions.execute(
                    () -> {
                      assertThat(stock.tryDecrease(sku, duplicate.quantity())).isTrue();
                      reservations.insert(duplicate);
                      return duplicate;
                    }))
        .isInstanceOf(ReservationAlreadyExistsException.class);

    assertThat(database.availableStock(sku)).isEqualTo(5);
    assertThat(reservations.findById(original.id())).contains(original);
    assertThat(database.reservationCount()).isOne();
  }

  @Test
  void rollsBackReleaseAndRestockWhenLaterOperationFails() {
    Sku sku = new Sku("SKU-ROLLBACK-RELEASE");
    Reservation confirmed =
        ReservationBuilder.aReservation().withSku(sku.value()).withQuantity(2).confirmed();
    Reservation released = confirmed.release(Instant.parse("2026-01-15T11:00:00Z"));
    database.insertStock(sku, 5);
    reservations.insert(confirmed);

    assertThatThrownBy(
            () ->
                transactions.execute(
                    () -> {
                      reservations.update(released);
                      stock.increase(sku, released.quantity());
                      throw new IllegalStateException("simulated downstream failure");
                    }))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("simulated downstream failure");

    assertThat(database.availableStock(sku)).isEqualTo(5);
    assertThat(reservations.findById(confirmed.id())).contains(confirmed);
  }

  @Test
  void preventsOversellingAcrossConcurrentTransactions()
      throws InterruptedException, ExecutionException, TimeoutException {
    Sku sku = new Sku("SKU-CONCURRENT-POSTGRES");
    database.insertStock(sku, INITIAL_STOCK);
    ReservationService service = reservationServiceFor(sku);
    ReserveStockCommand command = new ReserveStockCommand(sku.value(), 1);
    CountDownLatch ready = new CountDownLatch(CONCURRENT_ATTEMPTS);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<Boolean>> results = new ArrayList<>(CONCURRENT_ATTEMPTS);

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int index = 0; index < CONCURRENT_ATTEMPTS; index++) {
        results.add(
            executor.submit(
                () -> {
                  ready.countDown();
                  if (!start.await(COORDINATION_TIMEOUT_SECONDS, SECONDS)) {
                    throw new IllegalStateException("concurrent start signal timed out");
                  }
                  try {
                    service.reserve(command);
                    return true;
                  } catch (InsufficientStockException ignored) {
                    return false;
                  }
                }));
      }

      try {
        assertThat(ready.await(COORDINATION_TIMEOUT_SECONDS, SECONDS))
            .as("all virtual threads reached the start gate")
            .isTrue();
      } finally {
        start.countDown();
      }

      long accepted = 0;
      for (Future<Boolean> result : results) {
        if (result.get(COORDINATION_TIMEOUT_SECONDS, SECONDS)) {
          accepted++;
        }
      }

      assertThat(accepted).isEqualTo(INITIAL_STOCK);
    }

    assertThat(database.availableStock(sku)).isZero();
    assertThat(database.reservationCount()).isEqualTo(INITIAL_STOCK);
  }

  private ReservationService reservationServiceFor(Sku sku) {
    StubCatalogGateway catalog =
        new StubCatalogGateway().withStatus(sku, CatalogProductStatus.ACTIVE);
    Clock clock = Clock.fixed(ReservationBuilder.DEFAULT_CREATED_AT, ZoneOffset.UTC);
    return new ReservationService(
        catalog,
        stock,
        reservations,
        transactions,
        new DeterministicReservationIdGenerator(),
        clock);
  }
}
