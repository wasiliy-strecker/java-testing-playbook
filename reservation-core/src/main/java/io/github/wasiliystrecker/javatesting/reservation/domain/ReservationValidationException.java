package io.github.wasiliystrecker.javatesting.reservation.domain;

import java.io.Serial;

public final class ReservationValidationException extends IllegalArgumentException {

  @Serial private static final long serialVersionUID = 1L;

  private final String field;

  public ReservationValidationException(String field, String message) {
    super(message);
    this.field = field;
  }

  public String field() {
    return field;
  }
}
