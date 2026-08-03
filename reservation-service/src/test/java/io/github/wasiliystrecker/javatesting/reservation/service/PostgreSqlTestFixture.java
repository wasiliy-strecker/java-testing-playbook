package io.github.wasiliystrecker.javatesting.reservation.service;

import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.util.Objects;
import org.springframework.jdbc.core.simple.JdbcClient;

final class PostgreSqlTestFixture {

  private final JdbcClient jdbc;

  PostgreSqlTestFixture(JdbcClient jdbc) {
    this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
  }

  void reset() {
    jdbc.sql("TRUNCATE TABLE reservations, stock_items").update();
  }

  void insertStock(Sku sku, int availableQuantity) {
    jdbc.sql(
            """
            INSERT INTO stock_items (sku, available_quantity)
            VALUES (:sku, :availableQuantity)
            """)
        .param("sku", sku.value())
        .param("availableQuantity", availableQuantity)
        .update();
  }

  int availableStock(Sku sku) {
    return jdbc.sql(
            """
            SELECT available_quantity
            FROM stock_items
            WHERE sku = :sku
            """)
        .param("sku", sku.value())
        .query(Integer.class)
        .optional()
        .orElse(0);
  }

  long reservationCount() {
    return jdbc.sql("SELECT COUNT(*) FROM reservations").query(Long.class).single();
  }
}
