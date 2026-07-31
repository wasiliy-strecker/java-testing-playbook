package io.github.wasiliystrecker.javatesting.reservation.application;

import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.io.Serial;

public final class ProductNotReservableException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final String sku;
  private final CatalogProductStatus productStatus;

  public ProductNotReservableException(Sku sku, CatalogProductStatus productStatus) {
    super("Product " + sku.value() + " is not reservable: " + productStatus);
    this.sku = sku.value();
    this.productStatus = productStatus;
  }

  public String sku() {
    return sku;
  }

  public CatalogProductStatus productStatus() {
    return productStatus;
  }
}
