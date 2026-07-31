package io.github.wasiliystrecker.javatesting.reservation.application;

import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record ReservationReceipt(
    UUID reservationId,
    String sku,
    int quantity,
    ReservationStatus status,
    Instant createdAt,
    Optional<Instant> releasedAt) {

  static ReservationReceipt from(Reservation reservation) {
    return new ReservationReceipt(
        reservation.id().value(),
        reservation.sku().value(),
        reservation.quantity().value(),
        reservation.status(),
        reservation.createdAt(),
        reservation.releasedAt());
  }
}
