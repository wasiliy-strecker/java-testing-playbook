package io.github.wasiliystrecker.javatesting.reservation.application.port;

import io.github.wasiliystrecker.javatesting.reservation.application.ReservationAlreadyExistsException;
import io.github.wasiliystrecker.javatesting.reservation.application.ReservationNotFoundException;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import java.util.Optional;

public interface ReservationRepository {

  /**
   * Stores a new reservation.
   *
   * @throws ReservationAlreadyExistsException when the identifier is already present
   */
  void insert(Reservation reservation);

  Optional<Reservation> findById(ReservationId reservationId);

  /**
   * Persists the lifecycle state of an existing reservation.
   *
   * @throws ReservationNotFoundException when the identifier is unknown
   */
  void update(Reservation reservation);
}
