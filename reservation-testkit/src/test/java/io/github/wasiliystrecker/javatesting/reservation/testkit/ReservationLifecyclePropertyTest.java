package io.github.wasiliystrecker.javatesting.reservation.testkit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationStatus;
import java.time.Instant;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;

class ReservationLifecyclePropertyTest {

  @Property(tries = 500, seed = "20260801")
  void releasingPreservesIdentityAndBecomesIdempotent(
      @ForAll @IntRange(min = 1, max = Quantity.MAXIMUM) int quantity,
      @ForAll @LongRange(min = 0, max = 86_400) long secondsAfterCreation) {
    Instant createdAt = ReservationBuilder.DEFAULT_CREATED_AT;
    Instant releasedAt = createdAt.plusSeconds(secondsAfterCreation);
    Reservation confirmed =
        ReservationBuilder.aReservation().withQuantity(quantity).createdAt(createdAt).confirmed();

    Reservation released = confirmed.release(releasedAt);
    Reservation releasedAgain = released.release(releasedAt.plusSeconds(1));

    assertThat(released.id()).isEqualTo(confirmed.id());
    assertThat(released.sku()).isEqualTo(confirmed.sku());
    assertThat(released.quantity()).isEqualTo(confirmed.quantity());
    assertThat(released.createdAt()).isEqualTo(confirmed.createdAt());
    assertThat(released.status()).isEqualTo(ReservationStatus.RELEASED);
    assertThat(released.releasedAt()).contains(releasedAt);
    assertThat(releasedAgain).isSameAs(released);
  }
}
