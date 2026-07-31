package io.github.wasiliystrecker.javatesting.reservation.application;

import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import java.io.Serial;
import java.util.UUID;

public final class ReservationNotFoundException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final UUID reservationId;

  public ReservationNotFoundException(ReservationId reservationId) {
    super("Reservation " + reservationId.value() + " was not found.");
    this.reservationId = reservationId.value();
  }

  public UUID reservationId() {
    return reservationId;
  }
}
