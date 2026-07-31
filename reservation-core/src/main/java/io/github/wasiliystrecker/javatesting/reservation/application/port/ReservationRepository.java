package io.github.wasiliystrecker.javatesting.reservation.application.port;

import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import java.util.Optional;

public interface ReservationRepository {

  void insert(Reservation reservation);

  Optional<Reservation> findById(ReservationId reservationId);

  void update(Reservation reservation);
}
