package io.github.wasiliystrecker.javatesting.reservation.domain;

import java.util.UUID;

public record ReservationId(UUID value) {

  public ReservationId {
    if (value == null) {
      throw new ReservationValidationException("reservationId", "Reservation ID must not be null.");
    }
  }
}
