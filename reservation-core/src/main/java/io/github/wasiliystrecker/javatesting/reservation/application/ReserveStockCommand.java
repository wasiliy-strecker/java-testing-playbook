package io.github.wasiliystrecker.javatesting.reservation.application;

public record ReserveStockCommand(String sku, int quantity) {}
