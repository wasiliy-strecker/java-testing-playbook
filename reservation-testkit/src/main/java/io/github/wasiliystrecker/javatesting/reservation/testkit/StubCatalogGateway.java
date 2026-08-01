package io.github.wasiliystrecker.javatesting.reservation.testkit;

import io.github.wasiliystrecker.javatesting.reservation.application.CatalogProductStatus;
import io.github.wasiliystrecker.javatesting.reservation.application.port.CatalogGateway;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** A configurable catalog stub that treats unconfigured products as unknown. */
public final class StubCatalogGateway implements CatalogGateway {

  private final ConcurrentMap<Sku, CatalogProductStatus> statuses = new ConcurrentHashMap<>();

  public StubCatalogGateway withStatus(Sku sku, CatalogProductStatus status) {
    statuses.put(
        Objects.requireNonNull(sku, "sku must not be null"),
        Objects.requireNonNull(status, "status must not be null"));
    return this;
  }

  @Override
  public CatalogProductStatus statusOf(Sku sku) {
    Objects.requireNonNull(sku, "sku must not be null");
    return statuses.getOrDefault(sku, CatalogProductStatus.UNKNOWN);
  }
}
