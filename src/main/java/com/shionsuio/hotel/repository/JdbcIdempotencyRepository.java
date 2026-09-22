package com.shionsuio.hotel.repository;

import com.shionsuio.hotel.domain.IdempotencyRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcIdempotencyRepository implements IdempotencyRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcIdempotencyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int insertIfAbsent(String key, String requestHash) {
        return jdbcTemplate.update("""
                INSERT INTO idempotency_keys (idempotency_key, request_hash)
                VALUES (?, ?)
                ON CONFLICT (idempotency_key) DO NOTHING
                """, key, requestHash);
    }

    @Override
    public IdempotencyRecord findByKey(String key) {
        return jdbcTemplate.query("""
                SELECT idempotency_key, request_hash, reservation_id
                FROM idempotency_keys
                WHERE idempotency_key = ?
                """, (rs, rowNum) -> new IdempotencyRecord(
                rs.getString("idempotency_key"),
                rs.getString("request_hash"),
                rs.getObject("reservation_id", Long.class)
        ), key).stream().findFirst().orElse(null);
    }

    @Override
    public void attachReservation(String key, Long reservationId) {
        jdbcTemplate.update("""
                UPDATE idempotency_keys
                SET reservation_id = ?
                WHERE idempotency_key = ?
                """, reservationId, key);
    }
}
