package io.github.wasiliystrecker.javatesting.reservation.application.port;

import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;

public interface StockGateway {

  boolean tryDecrease(Sku sku, Quantity quantity);

  void increase(Sku sku, Quantity quantity);
}
