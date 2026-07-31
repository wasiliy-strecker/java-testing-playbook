package io.github.wasiliystrecker.javatesting.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SkuTest {

  @Test
  void normalizesWhitespaceAndCase() {
    assertThat(new Sku("  book-42  ").value()).isEqualTo("BOOK-42");
  }

  @Test
  void rejectsBlankSkuWithFieldInformation() {
    assertThatThrownBy(() -> new Sku("  "))
        .isInstanceOf(ReservationValidationException.class)
        .extracting("field")
        .isEqualTo("sku");
  }

  @Test
  void rejectsUnsupportedCharacters() {
    assertThatThrownBy(() -> new Sku("BOOK/42"))
        .isInstanceOf(ReservationValidationException.class)
        .hasMessageContaining("letters, digits");
  }
}
