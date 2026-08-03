package io.github.wasiliystrecker.javatesting.reservation.application;

import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import java.io.Serial;
import java.util.UUID;

public final class ReservationAlreadyExistsException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final UUID reservationId;

  public ReservationAlreadyExistsException(ReservationId reservationId) {
    this(reservationId, null);
  }

  public ReservationAlreadyExistsException(ReservationId reservationId, Throwable cause) {
    super("Reservation " + reservationId.value() + " already exists.", cause);
    this.reservationId = reservationId.value();
  }

  public UUID reservationId() {
    return reservationId;
  }
}
