package io.github.wasiliystrecker.javatesting.reservation.testkit;

import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationIdGenerator;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/** Generates stable reservation identifiers without relying on global random state. */
public final class DeterministicReservationIdGenerator implements ReservationIdGenerator {

  private final AtomicLong sequence;

  public DeterministicReservationIdGenerator() {
    this(1L);
  }

  public DeterministicReservationIdGenerator(long firstSequenceNumber) {
    if (firstSequenceNumber < 1L) {
      throw new IllegalArgumentException("firstSequenceNumber must be positive");
    }
    sequence = new AtomicLong(firstSequenceNumber);
  }

  @Override
  public ReservationId nextId() {
    return new ReservationId(new UUID(0L, sequence.getAndIncrement()));
  }
}
