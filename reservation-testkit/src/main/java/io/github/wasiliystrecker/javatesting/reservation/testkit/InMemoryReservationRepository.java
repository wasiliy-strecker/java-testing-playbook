package io.github.wasiliystrecker.javatesting.reservation.testkit;

import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** A thread-safe fake that preserves insert and update semantics for application tests. */
public final class InMemoryReservationRepository implements ReservationRepository {

  private final ConcurrentMap<ReservationId, Reservation> reservations = new ConcurrentHashMap<>();

  @Override
  public void insert(Reservation reservation) {
    Objects.requireNonNull(reservation, "reservation must not be null");
    Reservation previous = reservations.putIfAbsent(reservation.id(), reservation);
    if (previous != null) {
      throw new IllegalStateException("Reservation already exists: " + reservation.id().value());
    }
  }

  @Override
  public Optional<Reservation> findById(ReservationId reservationId) {
    Objects.requireNonNull(reservationId, "reservationId must not be null");
    return Optional.ofNullable(reservations.get(reservationId));
  }

  @Override
  public void update(Reservation reservation) {
    Objects.requireNonNull(reservation, "reservation must not be null");
    Reservation previous = reservations.replace(reservation.id(), reservation);
    if (previous == null) {
      throw new IllegalStateException("Reservation does not exist: " + reservation.id().value());
    }
  }

  public int size() {
    return reservations.size();
  }
}
