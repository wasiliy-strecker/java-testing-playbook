package io.github.wasiliystrecker.javatesting.reservation.application;

import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.io.Serial;

public final class StockItemNotFoundException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final String sku;

  public StockItemNotFoundException(Sku sku) {
    super("Stock item " + sku.value() + " was not found.");
    this.sku = sku.value();
  }

  public String sku() {
    return sku;
  }
}
