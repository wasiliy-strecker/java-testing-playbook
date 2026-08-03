package io.github.wasiliystrecker.javatesting.reservation.service;

import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import io.github.wasiliystrecker.javatesting.reservation.testkit.ReservationBuilder;
import io.github.wasiliystrecker.javatesting.reservation.testkit.contract.ReservationRepositoryContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest(classes = ReservationServiceApplication.class)
@Import(TestPostgreSqlConfiguration.class)
class JdbcReservationRepositoryContractIT extends ReservationRepositoryContract {

  private final ReservationRepository repository;
  private final PostgreSqlTestFixture database;

  @Autowired
  JdbcReservationRepositoryContractIT(ReservationRepository repository, JdbcClient jdbc) {
    this.repository = repository;
    database = new PostgreSqlTestFixture(jdbc);
  }

  @Override
  protected ReservationRepository repository() {
    return repository;
  }

  @Override
  protected void resetRepository() {
    database.reset();
    database.insertStock(new Sku(ReservationBuilder.DEFAULT_SKU), 100);
  }
}
