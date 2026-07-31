package io.github.wasiliystrecker.javatesting.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class QuantityTest {

  @Test
  void acceptsDocumentedBoundaries() {
    assertThat(new Quantity(1).value()).isEqualTo(1);
    assertThat(new Quantity(Quantity.MAXIMUM).value()).isEqualTo(Quantity.MAXIMUM);
  }

  @Test
  void rejectsZero() {
    assertThatThrownBy(() -> new Quantity(0))
        .isInstanceOf(ReservationValidationException.class)
        .extracting("field")
        .isEqualTo("quantity");
  }

  @Test
  void rejectsQuantityAboveMaximum() {
    assertThatThrownBy(() -> new Quantity(Quantity.MAXIMUM + 1))
        .isInstanceOf(ReservationValidationException.class)
        .hasMessageContaining("between 1 and " + Quantity.MAXIMUM);
  }
}
