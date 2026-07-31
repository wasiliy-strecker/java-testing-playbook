package io.github.wasiliystrecker.javatesting.reservation.application;

import io.github.wasiliystrecker.javatesting.reservation.application.port.CatalogGateway;
import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationIdGenerator;
import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.application.port.StockGateway;
import io.github.wasiliystrecker.javatesting.reservation.application.port.TransactionRunner;
import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationStatus;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ReservationService implements ReservationOperations {

  private final CatalogGateway catalog;
  private final StockGateway stock;
  private final ReservationRepository reservations;
  private final TransactionRunner transactions;
  private final ReservationIdGenerator idGenerator;
  private final Clock clock;

  public ReservationService(
      CatalogGateway catalog,
      StockGateway stock,
      ReservationRepository reservations,
      TransactionRunner transactions,
      ReservationIdGenerator idGenerator,
      Clock clock) {
    this.catalog = Objects.requireNonNull(catalog, "catalog must not be null");
    this.stock = Objects.requireNonNull(stock, "stock must not be null");
    this.reservations = Objects.requireNonNull(reservations, "reservations must not be null");
    this.transactions = Objects.requireNonNull(transactions, "transactions must not be null");
    this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator must not be null");
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
  }

  @Override
  public ReservationReceipt reserve(ReserveStockCommand command) {
    Objects.requireNonNull(command, "command must not be null");
    Sku sku = new Sku(command.sku());
    Quantity quantity = new Quantity(command.quantity());

    CatalogProductStatus productStatus = catalog.statusOf(sku);
    if (productStatus != CatalogProductStatus.ACTIVE) {
      throw new ProductNotReservableException(sku, productStatus);
    }

    Reservation reservation =
        Reservation.confirmed(idGenerator.nextId(), sku, quantity, clock.instant());

    return transactions.execute(
        () -> {
          if (!stock.tryDecrease(sku, quantity)) {
            throw new InsufficientStockException(sku, quantity);
          }
          reservations.insert(reservation);
          return ReservationReceipt.from(reservation);
        });
  }

  @Override
  public Optional<ReservationReceipt> findById(UUID reservationId) {
    return reservations.findById(new ReservationId(reservationId)).map(ReservationReceipt::from);
  }

  @Override
  public ReservationReceipt release(UUID reservationId) {
    ReservationId id = new ReservationId(reservationId);

    return transactions.execute(
        () -> {
          Reservation current =
              reservations.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
          if (current.status() == ReservationStatus.RELEASED) {
            return ReservationReceipt.from(current);
          }

          Reservation released = current.release(clock.instant());
          reservations.update(released);
          stock.increase(released.sku(), released.quantity());
          return ReservationReceipt.from(released);
        });
  }
}
