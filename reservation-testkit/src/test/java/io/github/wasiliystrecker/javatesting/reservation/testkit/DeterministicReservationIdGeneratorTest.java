package io.github.wasiliystrecker.javatesting.reservation.testkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeterministicReservationIdGeneratorTest {

  @Test
  void generatesAStableSequenceFromTheConfiguredStartingPoint() {
    DeterministicReservationIdGenerator generator = new DeterministicReservationIdGenerator(41L);

    assertThat(generator.nextId().value()).isEqualTo(new UUID(0L, 41L));
    assertThat(generator.nextId().value()).isEqualTo(new UUID(0L, 42L));
  }

  @Test
  void rejectsNonPositiveStartingPoints() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new DeterministicReservationIdGenerator(0L))
        .withMessage("firstSequenceNumber must be positive");
  }
}
