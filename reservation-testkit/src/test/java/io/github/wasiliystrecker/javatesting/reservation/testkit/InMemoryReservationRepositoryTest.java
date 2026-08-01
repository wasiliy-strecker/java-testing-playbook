package io.github.wasiliystrecker.javatesting.reservation.testkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryReservationRepositoryTest {

  @Test
  void preservesInsertLookupAndUpdateSemantics() {
    InMemoryReservationRepository repository = new InMemoryReservationRepository();
    Reservation confirmed = ReservationBuilder.aReservation().confirmed();
    Reservation released = confirmed.release(Instant.parse("2026-01-15T11:00:00Z"));

    repository.insert(confirmed);
    repository.update(released);

    assertThat(repository.findById(confirmed.id())).contains(released);
    assertThat(repository.size()).isOne();
  }

  @Test
  void rejectsDuplicateInserts() {
    InMemoryReservationRepository repository = new InMemoryReservationRepository();
    Reservation reservation = ReservationBuilder.aReservation().confirmed();
    repository.insert(reservation);

    assertThatIllegalStateException()
        .isThrownBy(() -> repository.insert(reservation))
        .withMessageContaining(reservation.id().value().toString());
  }

  @Test
  void rejectsUpdatesForUnknownReservations() {
    InMemoryReservationRepository repository = new InMemoryReservationRepository();
    Reservation unknown =
        ReservationBuilder.aReservation()
            .withId(UUID.fromString("00000000-0000-0000-0000-000000000099"))
            .confirmed();

    assertThatIllegalStateException()
        .isThrownBy(() -> repository.update(unknown))
        .withMessageContaining(unknown.id().value().toString());
  }
}
