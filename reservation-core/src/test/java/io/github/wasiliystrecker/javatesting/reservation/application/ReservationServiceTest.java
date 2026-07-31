package io.github.wasiliystrecker.javatesting.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.wasiliystrecker.javatesting.reservation.application.port.CatalogGateway;
import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.application.port.StockGateway;
import io.github.wasiliystrecker.javatesting.reservation.application.port.TransactionRunner;
import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationStatus;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ReservationServiceTest {

  private static final UUID RESERVATION_ID =
      UUID.fromString("018f1552-7b91-7cc4-b721-95c852d85d4f");
  private static final Instant NOW = Instant.parse("2026-07-31T08:00:00Z");

  private final InMemoryReservations reservations = new InMemoryReservations();
  private final StockLedger stock = new StockLedger();
  private final CountingTransactions transactions = new CountingTransactions();
  private CatalogProductStatus productStatus;
  private ReservationService service;

  @BeforeEach
  void setUp() {
    productStatus = CatalogProductStatus.ACTIVE;
    CatalogGateway catalog = ignored -> productStatus;
    service =
        new ReservationService(
            catalog,
            stock,
            reservations,
            transactions,
            () -> new ReservationId(RESERVATION_ID),
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void reservesActiveProductWithDeterministicIdentityAndTime() {
    stock.setAvailable("BOOK-42", 5);

    ReservationReceipt receipt = service.reserve(new ReserveStockCommand(" book-42 ", 2));

    assertThat(receipt.reservationId()).isEqualTo(RESERVATION_ID);
    assertThat(receipt.sku()).isEqualTo("BOOK-42");
    assertThat(receipt.quantity()).isEqualTo(2);
    assertThat(receipt.status()).isEqualTo(ReservationStatus.CONFIRMED);
    assertThat(receipt.createdAt()).isEqualTo(NOW);
    assertThat(stock.available("BOOK-42")).isEqualTo(3);
    assertThat(reservations.findById(new ReservationId(RESERVATION_ID))).isPresent();
    assertThat(transactions.invocations).isEqualTo(1);
  }

  @ParameterizedTest
  @EnumSource(
      value = CatalogProductStatus.class,
      names = {"DISCONTINUED", "UNKNOWN"})
  void rejectsUnreservableProductBeforeStartingTransaction(CatalogProductStatus status) {
    productStatus = status;
    stock.setAvailable("BOOK-42", 5);

    assertThatThrownBy(() -> service.reserve(new ReserveStockCommand("BOOK-42", 2)))
        .isInstanceOf(ProductNotReservableException.class)
        .extracting("productStatus")
        .isEqualTo(status);

    assertThat(transactions.invocations).isZero();
    assertThat(stock.available("BOOK-42")).isEqualTo(5);
    assertThat(reservations.entries).isEmpty();
  }

  @Test
  void rejectsReservationWhenStockCannotBeDecreased() {
    stock.setAvailable("BOOK-42", 1);

    assertThatThrownBy(() -> service.reserve(new ReserveStockCommand("BOOK-42", 2)))
        .isInstanceOf(InsufficientStockException.class)
        .extracting("requestedQuantity")
        .isEqualTo(2);

    assertThat(stock.available("BOOK-42")).isEqualTo(1);
    assertThat(reservations.entries).isEmpty();
    assertThat(transactions.invocations).isEqualTo(1);
  }

  @Test
  void releasesConfirmedReservationAndRestoresStockExactlyOnce() {
    Reservation confirmed =
        Reservation.confirmed(
            new ReservationId(RESERVATION_ID),
            new Sku("BOOK-42"),
            new Quantity(2),
            NOW.minusSeconds(60));
    reservations.insert(confirmed);
    stock.setAvailable("BOOK-42", 3);

    ReservationReceipt first = service.release(RESERVATION_ID);
    ReservationReceipt repeated = service.release(RESERVATION_ID);

    assertThat(first.status()).isEqualTo(ReservationStatus.RELEASED);
    assertThat(first.releasedAt()).contains(NOW);
    assertThat(repeated).isEqualTo(first);
    assertThat(stock.available("BOOK-42")).isEqualTo(5);
    assertThat(stock.increaseInvocations).isEqualTo(1);
    assertThat(reservations.updateInvocations).isEqualTo(1);
    assertThat(transactions.invocations).isEqualTo(2);
  }

  @Test
  void reportsMissingReservationFromReleaseUseCase() {
    assertThatThrownBy(() -> service.release(RESERVATION_ID))
        .isInstanceOf(ReservationNotFoundException.class)
        .extracting("reservationId")
        .isEqualTo(RESERVATION_ID);
  }

  @Test
  void findsExistingReservationWithoutOpeningTransaction() {
    Reservation confirmed =
        Reservation.confirmed(
            new ReservationId(RESERVATION_ID), new Sku("BOOK-42"), new Quantity(2), NOW);
    reservations.insert(confirmed);

    Optional<ReservationReceipt> found = service.findById(RESERVATION_ID);

    assertThat(found).contains(ReservationReceipt.from(confirmed));
    assertThat(transactions.invocations).isZero();
  }

  private static final class InMemoryReservations implements ReservationRepository {

    private final Map<ReservationId, Reservation> entries = new HashMap<>();
    private int updateInvocations;

    @Override
    public void insert(Reservation reservation) {
      entries.put(reservation.id(), reservation);
    }

    @Override
    public Optional<Reservation> findById(ReservationId reservationId) {
      return Optional.ofNullable(entries.get(reservationId));
    }

    @Override
    public void update(Reservation reservation) {
      updateInvocations++;
      entries.put(reservation.id(), reservation);
    }
  }

  private static final class StockLedger implements StockGateway {

    private final Map<Sku, Integer> available = new HashMap<>();
    private int increaseInvocations;

    void setAvailable(String sku, int quantity) {
      available.put(new Sku(sku), quantity);
    }

    int available(String sku) {
      return available.getOrDefault(new Sku(sku), 0);
    }

    @Override
    public boolean tryDecrease(Sku sku, Quantity quantity) {
      int current = available.getOrDefault(sku, 0);
      if (current < quantity.value()) {
        return false;
      }
      available.put(sku, current - quantity.value());
      return true;
    }

    @Override
    public void increase(Sku sku, Quantity quantity) {
      increaseInvocations++;
      available.merge(sku, quantity.value(), Integer::sum);
    }
  }

  private static final class CountingTransactions implements TransactionRunner {

    private int invocations;

    @Override
    public <T> T execute(TransactionalOperation<T> operation) {
      invocations++;
      return operation.execute();
    }
  }
}
