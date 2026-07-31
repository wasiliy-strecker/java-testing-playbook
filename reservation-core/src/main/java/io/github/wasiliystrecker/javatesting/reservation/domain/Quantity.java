package io.github.wasiliystrecker.javatesting.reservation.domain;

public record Quantity(int value) {

  public static final int MAXIMUM = 100;

  public Quantity {
    if (value < 1 || value > MAXIMUM) {
      throw new ReservationValidationException(
          "quantity", "Quantity must be between 1 and " + MAXIMUM + ".");
    }
  }
}
