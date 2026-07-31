package io.github.wasiliystrecker.javatesting.reservation.application.port;

import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;

@FunctionalInterface
public interface ReservationIdGenerator {

  ReservationId nextId();
}
