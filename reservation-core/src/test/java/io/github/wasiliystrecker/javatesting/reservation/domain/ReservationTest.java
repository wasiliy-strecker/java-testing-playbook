package io.github.wasiliystrecker.javatesting.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReservationTest {

  private static final ReservationId ID =
      new ReservationId(UUID.fromString("018f1552-7b91-7cc4-b721-95c852d85d4f"));
  private static final Instant CREATED_AT = Instant.parse("2026-07-31T08:00:00Z");

  @Test
  void createsConfirmedReservationWithoutReleaseTime() {
    Reservation reservation =
        Reservation.confirmed(ID, new Sku("BOOK-42"), new Quantity(2), CREATED_AT);

    assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
    assertThat(reservation.releasedAt()).isEmpty();
  }

  @Test
  void releasesReservationOnce() {
    Reservation reservation =
        Reservation.confirmed(ID, new Sku("BOOK-42"), new Quantity(2), CREATED_AT);
    Instant releasedAt = CREATED_AT.plusSeconds(60);

    Reservation released = reservation.release(releasedAt);

    assertThat(released.status()).isEqualTo(ReservationStatus.RELEASED);
    assertThat(released.releasedAt()).contains(releasedAt);
    assertThat(released.release(releasedAt.plusSeconds(60))).isSameAs(released);
  }

  @Test
  void rejectsReleaseBeforeCreation() {
    Reservation reservation =
        Reservation.confirmed(ID, new Sku("BOOK-42"), new Quantity(2), CREATED_AT);

    assertThatThrownBy(() -> reservation.release(CREATED_AT.minusSeconds(1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Release time cannot be before creation time.");
  }
}
