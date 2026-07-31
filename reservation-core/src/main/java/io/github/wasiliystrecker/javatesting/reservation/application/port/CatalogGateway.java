package io.github.wasiliystrecker.javatesting.reservation.application.port;

import io.github.wasiliystrecker.javatesting.reservation.application.CatalogProductStatus;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;

@FunctionalInterface
public interface CatalogGateway {

  CatalogProductStatus statusOf(Sku sku);
}
