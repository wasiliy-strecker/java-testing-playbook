package io.github.wasiliystrecker.javatesting.reservation.testkit;

import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Creates valid reservations with explicit overrides for behavior-focused tests. */
public final class ReservationBuilder {

  public static final UUID DEFAULT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final String DEFAULT_SKU = "SKU-001";
  public static final Instant DEFAULT_CREATED_AT = Instant.parse("2026-01-15T10:00:00Z");

  private ReservationId id = new ReservationId(DEFAULT_ID);
  private Sku sku = new Sku(DEFAULT_SKU);
  private Quantity quantity = new Quantity(1);
  private Instant createdAt = DEFAULT_CREATED_AT;

  private ReservationBuilder() {}

  public static ReservationBuilder aReservation() {
    return new ReservationBuilder();
  }

  public ReservationBuilder withId(UUID value) {
    id = new ReservationId(Objects.requireNonNull(value, "value must not be null"));
    return this;
  }

  public ReservationBuilder withSku(String value) {
    sku = new Sku(value);
    return this;
  }

  public ReservationBuilder withQuantity(int value) {
    quantity = new Quantity(value);
    return this;
  }

  public ReservationBuilder createdAt(Instant value) {
    createdAt = Objects.requireNonNull(value, "value must not be null");
    return this;
  }

  public Reservation confirmed() {
    return Reservation.confirmed(id, sku, quantity, createdAt);
  }

  public Reservation releasedAt(Instant releaseTime) {
    return confirmed().release(releaseTime);
  }
}
