package io.github.wasiliystrecker.javatesting.reservation.testkit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** A controllable clock whose zone views share the same timeline. */
public final class MutableClock extends Clock {

  private final AtomicReference<Instant> currentInstant;
  private final ZoneId zone;

  public static MutableClock startingAt(Instant instant) {
    return new MutableClock(instant, ZoneOffset.UTC);
  }

  public MutableClock(Instant instant, ZoneId zone) {
    this(new AtomicReference<>(Objects.requireNonNull(instant, "instant must not be null")), zone);
  }

  private MutableClock(AtomicReference<Instant> currentInstant, ZoneId zone) {
    this.currentInstant = currentInstant;
    this.zone = Objects.requireNonNull(zone, "zone must not be null");
  }

  public void set(Instant instant) {
    currentInstant.set(Objects.requireNonNull(instant, "instant must not be null"));
  }

  public Instant advance(Duration duration) {
    Objects.requireNonNull(duration, "duration must not be null");
    return currentInstant.updateAndGet(instant -> instant.plus(duration));
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public Clock withZone(ZoneId requestedZone) {
    Objects.requireNonNull(requestedZone, "requestedZone must not be null");
    if (zone.equals(requestedZone)) {
      return this;
    }
    return new MutableClock(currentInstant, requestedZone);
  }

  @Override
  public Instant instant() {
    return currentInstant.get();
  }
}
