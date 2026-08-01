package io.github.wasiliystrecker.javatesting.reservation.testkit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.wasiliystrecker.javatesting.reservation.application.CatalogProductStatus;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import org.junit.jupiter.api.Test;

class TestDoublesTest {

  @Test
  void catalogStubMakesItsFallbackAndOverridesExplicit() {
    Sku configured = new Sku("SKU-ACTIVE");
    Sku unconfigured = new Sku("SKU-UNKNOWN");
    StubCatalogGateway catalog =
        new StubCatalogGateway().withStatus(configured, CatalogProductStatus.ACTIVE);

    assertThat(catalog.statusOf(configured)).isEqualTo(CatalogProductStatus.ACTIVE);
    assertThat(catalog.statusOf(unconfigured)).isEqualTo(CatalogProductStatus.UNKNOWN);
  }

  @Test
  void transactionRunnerExecutesInlineAndExposesTheBoundary() {
    RecordingTransactionRunner transactions = new RecordingTransactionRunner();

    String result = transactions.execute(() -> "committed");

    assertThat(result).isEqualTo("committed");
    assertThat(transactions.invocationCount()).isOne();
  }
}
