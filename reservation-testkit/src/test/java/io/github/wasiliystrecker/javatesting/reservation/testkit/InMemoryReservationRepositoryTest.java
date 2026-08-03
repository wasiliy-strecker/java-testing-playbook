package io.github.wasiliystrecker.javatesting.reservation.testkit;

import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.testkit.contract.ReservationRepositoryContract;

class InMemoryReservationRepositoryTest extends ReservationRepositoryContract {

  private InMemoryReservationRepository repository;

  @Override
  protected ReservationRepository repository() {
    return repository;
  }

  @Override
  protected void resetRepository() {
    repository = new InMemoryReservationRepository();
  }
}
