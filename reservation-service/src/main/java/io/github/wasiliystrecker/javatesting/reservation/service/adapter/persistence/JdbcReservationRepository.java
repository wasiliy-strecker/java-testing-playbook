package io.github.wasiliystrecker.javatesting.reservation.service.adapter.persistence;

import io.github.wasiliystrecker.javatesting.reservation.application.ReservationAlreadyExistsException;
import io.github.wasiliystrecker.javatesting.reservation.application.ReservationNotFoundException;
import io.github.wasiliystrecker.javatesting.reservation.application.port.ReservationRepository;
import io.github.wasiliystrecker.javatesting.reservation.domain.Quantity;
import io.github.wasiliystrecker.javatesting.reservation.domain.Reservation;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationId;
import io.github.wasiliystrecker.javatesting.reservation.domain.ReservationStatus;
import io.github.wasiliystrecker.javatesting.reservation.domain.Sku;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

  private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER =
      JdbcReservationRepository::mapReservation;

  private final JdbcClient jdbc;

  public JdbcReservationRepository(JdbcClient jdbc) {
    this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
  }

  @Override
  public void insert(Reservation reservation) {
    Objects.requireNonNull(reservation, "reservation must not be null");
    try {
      jdbc.sql(
              """
              INSERT INTO reservations (
                  reservation_id, sku, quantity, reservation_status, created_at, released_at
              ) VALUES (
                  :reservationId, :sku, :quantity, :status, :createdAt, :releasedAt
              )
              """)
          .param("reservationId", reservation.id().value())
          .param("sku", reservation.sku().value())
          .param("quantity", reservation.quantity().value())
          .param("status", reservation.status().name())
          .param("createdAt", toOffsetDateTime(reservation.createdAt()))
          .param(
              "releasedAt",
              reservation
                  .releasedAt()
                  .map(JdbcReservationRepository::toOffsetDateTime)
                  .orElse(null),
              Types.TIMESTAMP_WITH_TIMEZONE)
          .update();
    } catch (DuplicateKeyException exception) {
      throw new ReservationAlreadyExistsException(reservation.id(), exception);
    }
  }

  @Override
  public Optional<Reservation> findById(ReservationId reservationId) {
    Objects.requireNonNull(reservationId, "reservationId must not be null");
    return jdbc.sql(
            """
            SELECT reservation_id, sku, quantity, reservation_status, created_at, released_at
            FROM reservations
            WHERE reservation_id = :reservationId
            """)
        .param("reservationId", reservationId.value())
        .query(RESERVATION_ROW_MAPPER)
        .optional();
  }

  @Override
  public void update(Reservation reservation) {
    Objects.requireNonNull(reservation, "reservation must not be null");
    int updatedRows =
        jdbc.sql(
                """
                UPDATE reservations
                SET reservation_status = :status,
                    released_at = :releasedAt
                WHERE reservation_id = :reservationId
                """)
            .param("status", reservation.status().name())
            .param(
                "releasedAt",
                reservation
                    .releasedAt()
                    .map(JdbcReservationRepository::toOffsetDateTime)
                    .orElse(null),
                Types.TIMESTAMP_WITH_TIMEZONE)
            .param("reservationId", reservation.id().value())
            .update();

    if (updatedRows != 1) {
      throw new ReservationNotFoundException(reservation.id());
    }
  }

  private static Reservation mapReservation(ResultSet resultSet, int rowNumber)
      throws SQLException {
    OffsetDateTime releasedAt = resultSet.getObject("released_at", OffsetDateTime.class);
    return new Reservation(
        new ReservationId(resultSet.getObject("reservation_id", java.util.UUID.class)),
        new Sku(resultSet.getString("sku")),
        new Quantity(resultSet.getInt("quantity")),
        ReservationStatus.valueOf(resultSet.getString("reservation_status")),
        resultSet.getObject("created_at", OffsetDateTime.class).toInstant(),
        Optional.ofNullable(releasedAt).map(OffsetDateTime::toInstant));
  }

  private static OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
    return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
  }
}
