package io.github.wasiliystrecker.javatesting.reservation.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record Reservation(
    ReservationId id,
    Sku sku,
    Quantity quantity,
    ReservationStatus status,
    Instant createdAt,
    Optional<Instant> releasedAt) {

  public Reservation {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(sku, "sku must not be null");
    Objects.requireNonNull(quantity, "quantity must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(releasedAt, "releasedAt must not be null");

    if (status == ReservationStatus.CONFIRMED && releasedAt.isPresent()) {
      throw new IllegalArgumentException("A confirmed reservation cannot have a release time.");
    }
    if (status == ReservationStatus.RELEASED && releasedAt.isEmpty()) {
      throw new IllegalArgumentException("A released reservation requires a release time.");
    }
    if (releasedAt.filter(time -> time.isBefore(createdAt)).isPresent()) {
      throw new IllegalArgumentException("Release time cannot be before creation time.");
    }
  }

  public static Reservation confirmed(
      ReservationId id, Sku sku, Quantity quantity, Instant createdAt) {
    return new Reservation(
        id, sku, quantity, ReservationStatus.CONFIRMED, createdAt, Optional.empty());
  }

  public Reservation release(Instant releaseTime) {
    Objects.requireNonNull(releaseTime, "releaseTime must not be null");
    if (status == ReservationStatus.RELEASED) {
      return this;
    }
    return new Reservation(
        id, sku, quantity, ReservationStatus.RELEASED, createdAt, Optional.of(releaseTime));
  }
}
