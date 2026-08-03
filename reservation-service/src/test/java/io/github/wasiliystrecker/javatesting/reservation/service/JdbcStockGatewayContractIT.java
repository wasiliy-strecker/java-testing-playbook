package io.github.wasiliystrecker.javatesting.reservation.service;

import io.github.wasiliystrecker.javatesting.reservation.application.port.StockGateway;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import io.github.wasiliystrecker.javatesting.reservation.testkit.contract.StockGatewayContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest(classes = ReservationServiceApplication.class)
@Import(TestPostgreSqlConfiguration.class)
class JdbcStockGatewayContractIT extends StockGatewayContract {

  private final StockGateway stock;
  private final PostgreSqlTestFixture database;

  @Autowired
  JdbcStockGatewayContractIT(StockGateway stock, JdbcClient jdbc) {
    this.stock = stock;
    database = new PostgreSqlTestFixture(jdbc);
  }

  @Override
  protected StockGateway stockGateway() {
    return stock;
  }

  @Override
  protected void resetStock() {
    database.reset();
  }

  @Override
  protected void setAvailable(Sku sku, int available) {
    database.insertStock(sku, available);
  }

  @Override
  protected int available(Sku sku) {
    return database.availableStock(sku);
  }
}
