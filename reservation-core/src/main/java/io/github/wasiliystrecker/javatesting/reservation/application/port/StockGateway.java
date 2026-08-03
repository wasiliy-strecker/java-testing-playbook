package io.github.wasiliystrecker.javatesting.reservation.application.port;

import io.github.wasiliystrecker.javatesting.reservation.application.StockItemNotFoundException;
import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;

public interface StockGateway {

  /** Atomically decreases available stock when the requested quantity is present. */
  boolean tryDecrease(Sku sku, Quantity quantity);

  /**
   * Returns stock to an existing item.
   *
   * @throws StockItemNotFoundException when the SKU is unknown
   */
  void increase(Sku sku, Quantity quantity);
}
