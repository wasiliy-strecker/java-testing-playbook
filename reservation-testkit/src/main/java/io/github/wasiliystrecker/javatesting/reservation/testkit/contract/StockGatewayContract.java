package io.github.wasiliystrecker.javatesting.reservation.testkit.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.wasiliystrecker.javatesting.reservation.application.StockItemNotFoundException;
import io.github.wasiliystrecker.javatesting.reservation.application.port.StockGateway;
import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Reusable behavioral contract for every {@link StockGateway} implementation. */
public abstract class StockGatewayContract {

  protected static final Sku PRIMARY_SKU = new Sku("SKU-CONTRACT-A");
  protected static final Sku SECONDARY_SKU = new Sku("SKU-CONTRACT-B");

  protected abstract StockGateway stockGateway();

  protected abstract void resetStock();

  protected abstract void setAvailable(Sku sku, int available);

  protected abstract int available(Sku sku);

  @BeforeEach
  protected final void resetStockGatewayContract() {
    resetStock();
  }

  @Test
  protected final void decreasesStockWhenRequestedQuantityIsAvailable() {
    setAvailable(PRIMARY_SKU, 10);

    boolean decreased = stockGateway().tryDecrease(PRIMARY_SKU, new Quantity(4));

    assertThat(decreased).isTrue();
    assertThat(available(PRIMARY_SKU)).isEqualTo(6);
  }

  @Test
  protected final void leavesStockUnchangedWhenRequestedQuantityIsUnavailable() {
    setAvailable(PRIMARY_SKU, 3);

    boolean decreased = stockGateway().tryDecrease(PRIMARY_SKU, new Quantity(4));

    assertThat(decreased).isFalse();
    assertThat(available(PRIMARY_SKU)).isEqualTo(3);
  }

  @Test
  protected final void allowsStockToReachExactlyZero() {
    setAvailable(PRIMARY_SKU, 5);

    boolean decreased = stockGateway().tryDecrease(PRIMARY_SKU, new Quantity(5));

    assertThat(decreased).isTrue();
    assertThat(available(PRIMARY_SKU)).isZero();
  }

  @Test
  protected final void treatsUnknownStockAsUnavailable() {
    assertThat(stockGateway().tryDecrease(PRIMARY_SKU, new Quantity(1))).isFalse();
    assertThat(available(PRIMARY_SKU)).isZero();
  }

  @Test
  protected final void increasesExistingStock() {
    setAvailable(PRIMARY_SKU, 4);

    stockGateway().increase(PRIMARY_SKU, new Quantity(3));

    assertThat(available(PRIMARY_SKU)).isEqualTo(7);
  }

  @Test
  protected final void isolatesChangesBySku() {
    setAvailable(PRIMARY_SKU, 8);
    setAvailable(SECONDARY_SKU, 11);

    boolean decreased = stockGateway().tryDecrease(PRIMARY_SKU, new Quantity(3));

    assertThat(decreased).isTrue();
    assertThat(available(PRIMARY_SKU)).isEqualTo(5);
    assertThat(available(SECONDARY_SKU)).isEqualTo(11);
  }

  @Test
  protected final void rejectsIncreaseForUnknownStockItem() {
    assertThatThrownBy(() -> stockGateway().increase(PRIMARY_SKU, new Quantity(1)))
        .isInstanceOfSatisfying(
            StockItemNotFoundException.class,
            exception -> assertThat(exception.sku()).isEqualTo(PRIMARY_SKU.value()));
  }
}
