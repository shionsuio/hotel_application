package com.shionsuio.hotel.repository;


import com.shionsuio.hotel.domain.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class JdbcRoomRepository implements RoomRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Room> findAvailableRoom(
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        String sql = """
                select r.id, r.room_number, r.price
                from rooms r
                where not exists (
                    select 1
                    from reservations res
                    where res.room_id = r.id
                        and res.status = 'CONFIRMED'
                        and res.check_in_date < ?
                        and res.check_out_date > ?
                )
                order by r.id
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Room(
                        rs.getLong("id"),
                        rs.getString("room_number"),
                        rs.getInt("price")
                ),
                Date.valueOf(checkOut),
                Date.valueOf(checkIn)
        );
    }

}
