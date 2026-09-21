package com.shionsuio.hotel.repository;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long save(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        String sql = """
                insert into reservations
                    (room_id, check_in_date, check_out_date, status)
                values (?,?,?,?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        //予約自体にIDを持たせ、そのIDをそのまま使用したいためkeyHolderを使用

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});

            ps.setLong(1, roomId);
            ps.setDate(2, Date.valueOf(checkIn));
            ps.setDate(3, Date.valueOf(checkOut));
            ps.setString(4, "CONFIRMED");

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("予約IDを取得できませんでした");
        }

        return key.longValue();

    }
}
