package io.github.wasiliystrecker.javatesting.reservation.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestPostgreSqlConfiguration {

  static final String POSTGRES_IMAGE = "postgres:17.10-alpine3.24";

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))
        .withDatabaseName("reservations")
        .withUsername("test")
        .withPassword("test");
  }
}
