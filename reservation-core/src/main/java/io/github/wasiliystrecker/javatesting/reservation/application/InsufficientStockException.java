package io.github.wasiliystrecker.javatesting.reservation.application;

import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.io.Serial;

public final class InsufficientStockException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final String sku;
  private final int requestedQuantity;

  public InsufficientStockException(Sku sku, Quantity requestedQuantity) {
    super(
        "Insufficient stock for "
            + sku.value()
            + " and quantity "
            + requestedQuantity.value()
            + ".");
    this.sku = sku.value();
    this.requestedQuantity = requestedQuantity.value();
  }

  public String sku() {
    return sku;
  }

  public int requestedQuantity() {
    return requestedQuantity;
  }
}
