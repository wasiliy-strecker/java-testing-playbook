package io.github.wasiliystrecker.javatesting.reservation.testkit.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.wasiliystrecker.javatesting.reservation.application.ReservationAlreadyExistsException;
import io.github.wasiliystrecker.javatesting.reservation.application.ReservationNotFoundException;
import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import io.github.wasiliystrecker.javatesting.reservation.testkit.ReservationBuilder;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Reusable behavioral contract for every {@link ReservationRepository} implementation. */
public abstract class ReservationRepositoryContract {

  private static final ReservationId UNKNOWN_ID =
      new ReservationId(UUID.fromString("00000000-0000-0000-0000-000000000099"));

  protected abstract ReservationRepository repository();

  protected abstract void resetRepository();

  @BeforeEach
  protected final void resetReservationRepositoryContract() {
    resetRepository();
  }

  @Test
  protected final void returnsEmptyWhenReservationIsUnknown() {
    assertThat(repository().findById(UNKNOWN_ID)).isEmpty();
  }

  @Test
  protected final void roundTripsConfirmedReservationWithoutDataLoss() {
    Reservation confirmed = ReservationBuilder.aReservation().withQuantity(7).confirmed();

    repository().insert(confirmed);

    assertThat(repository().findById(confirmed.id())).contains(confirmed);
  }

  @Test
  protected final void roundTripsReleasedReservationWithoutDataLoss() {
    Reservation released =
        ReservationBuilder.aReservation()
            .withQuantity(3)
            .releasedAt(Instant.parse("2026-01-15T11:00:00Z"));

    repository().insert(released);

    assertThat(repository().findById(released.id())).contains(released);
  }

  @Test
  protected final void updatesLifecycleStateOfExistingReservation() {
    Reservation confirmed = ReservationBuilder.aReservation().confirmed();
    Reservation released = confirmed.release(Instant.parse("2026-01-15T11:00:00Z"));
    repository().insert(confirmed);

    repository().update(released);

    assertThat(repository().findById(confirmed.id())).contains(released);
  }

  @Test
  protected final void rejectsDuplicateReservationIdentifier() {
    Reservation original = ReservationBuilder.aReservation().withQuantity(2).confirmed();
    Reservation duplicate =
        ReservationBuilder.aReservation().withId(original.id().value()).withQuantity(9).confirmed();
    repository().insert(original);

    assertThatThrownBy(() -> repository().insert(duplicate))
        .isInstanceOfSatisfying(
            ReservationAlreadyExistsException.class,
            exception -> assertThat(exception.reservationId()).isEqualTo(original.id().value()));
    assertThat(repository().findById(original.id())).contains(original);
  }

  @Test
  protected final void rejectsUpdateForUnknownReservation() {
    Reservation unknown = ReservationBuilder.aReservation().withId(UNKNOWN_ID.value()).confirmed();

    assertThatThrownBy(() -> repository().update(unknown))
        .isInstanceOfSatisfying(
            ReservationNotFoundException.class,
            exception -> assertThat(exception.reservationId()).isEqualTo(UNKNOWN_ID.value()));
    assertThat(repository().findById(UNKNOWN_ID)).isEmpty();
  }
}
