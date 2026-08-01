package io.github.wasiliystrecker.javatesting.reservation.testkit;

import io.github.wasiliystrecker.javatesting.reservation.application.port.StockGateway;
import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** A thread-safe stock fake with an atomic check-and-decrease operation. */
public final class InMemoryStockGateway implements StockGateway {

  private final ConcurrentMap<Sku, Integer> availableStock = new ConcurrentHashMap<>();

  public InMemoryStockGateway setAvailable(Sku sku, int available) {
    Objects.requireNonNull(sku, "sku must not be null");
    if (available < 0) {
      throw new IllegalArgumentException("available must not be negative");
    }
    availableStock.put(sku, available);
    return this;
  }

  public int available(Sku sku) {
    Objects.requireNonNull(sku, "sku must not be null");
    return availableStock.getOrDefault(sku, 0);
  }

  @Override
  public boolean tryDecrease(Sku sku, Quantity quantity) {
    Objects.requireNonNull(sku, "sku must not be null");
    Objects.requireNonNull(quantity, "quantity must not be null");
    AtomicBoolean decreased = new AtomicBoolean();

    availableStock.compute(
        sku,
        (ignored, current) -> {
          int available = current == null ? 0 : current;
          if (available < quantity.value()) {
            return available;
          }
          decreased.set(true);
          return available - quantity.value();
        });

    return decreased.get();
  }

  @Override
  public void increase(Sku sku, Quantity quantity) {
    Objects.requireNonNull(sku, "sku must not be null");
    Objects.requireNonNull(quantity, "quantity must not be null");
    availableStock.merge(sku, quantity.value(), Math::addExact);
  }
}
