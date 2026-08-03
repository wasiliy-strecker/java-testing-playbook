package io.github.wasiliystrecker.javatesting.reservation.service.adapter.persistence;

import io.github.wasiliystrecker.javatesting.reservation.application.StockItemNotFoundException;
import io.github.wasiliystrecker.javatesting.reservation.application.port.StockGateway;
import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.util.Objects;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcStockGateway implements StockGateway {

  private final JdbcClient jdbc;

  public JdbcStockGateway(JdbcClient jdbc) {
    this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
  }

  @Override
  public boolean tryDecrease(Sku sku, Quantity quantity) {
    Objects.requireNonNull(sku, "sku must not be null");
    Objects.requireNonNull(quantity, "quantity must not be null");
    int updatedRows =
        jdbc.sql(
                """
                UPDATE stock_items
                SET available_quantity = available_quantity - :quantity
                WHERE sku = :sku
                  AND available_quantity >= :quantity
                """)
            .param("sku", sku.value())
            .param("quantity", quantity.value())
            .update();
    return updatedRows == 1;
  }

  @Override
  public void increase(Sku sku, Quantity quantity) {
    Objects.requireNonNull(sku, "sku must not be null");
    Objects.requireNonNull(quantity, "quantity must not be null");
    int updatedRows =
        jdbc.sql(
                """
                UPDATE stock_items
                SET available_quantity = available_quantity + :quantity
                WHERE sku = :sku
                """)
            .param("sku", sku.value())
            .param("quantity", quantity.value())
            .update();

    if (updatedRows != 1) {
      throw new StockItemNotFoundException(sku);
    }
  }
}
