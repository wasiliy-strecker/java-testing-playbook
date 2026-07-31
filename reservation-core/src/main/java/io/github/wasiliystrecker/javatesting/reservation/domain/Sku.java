package io.github.wasiliystrecker.javatesting.reservation.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public record Sku(String value) {

  private static final Pattern VALID_SKU = Pattern.compile("[A-Z0-9][A-Z0-9._-]{2,63}");

  public Sku {
    if (value == null || value.isBlank()) {
      throw new ReservationValidationException("sku", "SKU must not be blank.");
    }

    value = value.strip().toUpperCase(Locale.ROOT);
    if (!VALID_SKU.matcher(value).matches()) {
      throw new ReservationValidationException(
          "sku", "SKU must contain 3 to 64 letters, digits, dots, underscores, or hyphens.");
    }
  }
}
