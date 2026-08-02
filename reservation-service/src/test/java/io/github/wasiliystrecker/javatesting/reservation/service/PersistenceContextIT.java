package io.github.wasiliystrecker.javatesting.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.application.port.StockGateway;
import io.github.wasiliystrecker.javatesting.reservation.application.port.TransactionRunner;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import io.github.wasiliystrecker.javatesting.reservation.testkit.ReservationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest(classes = ReservationServiceApplication.class)
@Import(TestPostgreSqlConfiguration.class)
class PersistenceContextIT {

  private final JdbcClient jdbc;
  private final ReservationRepository reservations;
  private final StockGateway stock;
  private final TransactionRunner transactions;

  @Autowired
  PersistenceContextIT(
      JdbcClient jdbc,
      ReservationRepository reservations,
      StockGateway stock,
      TransactionRunner transactions) {
    this.jdbc = jdbc;
    this.reservations = reservations;
    this.stock = stock;
    this.transactions = transactions;
  }

  @BeforeEach
  void resetDatabase() {
    jdbc.sql("TRUNCATE TABLE reservations, stock_items").update();
  }

  @Test
  void migratesSchemaAndPersistsStockChangeWithReservationInOneTransaction() {
    Sku sku = new Sku("BOOK-42");
    Reservation reservation =
        ReservationBuilder.aReservation().withSku(sku.value()).withQuantity(2).confirmed();
    insertStock(sku, 5);

    Reservation persisted =
        transactions.execute(
            () -> {
              assertThat(stock.tryDecrease(sku, reservation.quantity())).isTrue();
              reservations.insert(reservation);
              return reservation;
            });

    assertThat(persisted).isEqualTo(reservation);
    assertThat(reservations.findById(reservation.id())).contains(reservation);
    assertThat(availableStock(sku)).isEqualTo(3);
  }

  @Test
  void databaseConstraintRejectsNegativeStock() {
    assertThatThrownBy(() -> insertStock(new Sku("BOOK-42"), -1))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("stock_items_quantity_non_negative");
  }

  private void insertStock(Sku sku, int availableQuantity) {
    jdbc.sql(
            """
            INSERT INTO stock_items (sku, available_quantity)
            VALUES (:sku, :availableQuantity)
            """)
        .param("sku", sku.value())
        .param("availableQuantity", availableQuantity)
        .update();
  }

  private int availableStock(Sku sku) {
    return jdbc.sql(
            """
            SELECT available_quantity
            FROM stock_items
            WHERE sku = :sku
            """)
        .param("sku", sku.value())
        .query(Integer.class)
        .single();
  }
}
