package io.github.wasiliystrecker.javatesting.reservation.testkit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

class StockGatewayPropertyTest {

  private static final Sku SKU = new Sku("SKU-PROPERTY");

  @Property(tries = 500, seed = "20260802")
  void acceptedDecreasesConserveStockAndNeverProduceNegativeInventory(
      @ForAll @IntRange(min = 0, max = 10_000) int initialStock,
      @ForAll("requestedQuantities") List<Integer> requestedQuantities) {
    InMemoryStockGateway stock = new InMemoryStockGateway().setAvailable(SKU, initialStock);
    List<Quantity> accepted = new ArrayList<>();
    int acceptedUnits = 0;

    for (int requested : requestedQuantities) {
      Quantity quantity = new Quantity(requested);
      if (stock.tryDecrease(SKU, quantity)) {
        accepted.add(quantity);
        acceptedUnits += quantity.value();
      }

      assertThat(stock.available(SKU)).isNotNegative().isEqualTo(initialStock - acceptedUnits);
    }

    accepted.forEach(quantity -> stock.increase(SKU, quantity));
    assertThat(stock.available(SKU)).isEqualTo(initialStock);
  }

  @Provide
  Arbitrary<List<Integer>> requestedQuantities() {
    return Arbitraries.integers().between(1, Quantity.MAXIMUM).list().ofMaxSize(100);
  }
}
