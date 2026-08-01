package io.github.wasiliystrecker.javatesting.reservation.testkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class MutableClockTest {

  @Test
  void advancesWithoutWallClockWaiting() {
    MutableClock clock = MutableClock.startingAt(Instant.parse("2026-01-15T10:00:00Z"));

    Instant advancedTo = clock.advance(Duration.ofMinutes(15));

    assertThat(advancedTo).isEqualTo(Instant.parse("2026-01-15T10:15:00Z"));
    assertThat(clock.instant()).isEqualTo(advancedTo);
  }

  @Test
  void zoneViewsShareTheControlledTimeline() {
    MutableClock clock = MutableClock.startingAt(Instant.parse("2026-01-15T10:00:00Z"));
    Clock berlinView = clock.withZone(ZoneId.of("Europe/Berlin"));

    clock.set(Instant.parse("2026-01-16T08:30:00Z"));

    assertThat(berlinView.instant()).isEqualTo(clock.instant());
    assertThat(berlinView.getZone()).isEqualTo(ZoneId.of("Europe/Berlin"));
  }
}
