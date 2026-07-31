package io.github.wasiliystrecker.javatesting.reservation.application;

import java.util.Optional;
import java.util.UUID;

public interface ReservationOperations {

  ReservationReceipt reserve(ReserveStockCommand command);

  Optional<ReservationReceipt> findById(UUID reservationId);

  ReservationReceipt release(UUID reservationId);
}
