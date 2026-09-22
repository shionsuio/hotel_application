package com.shionsuio.hotel.service;

import com.shionsuio.hotel.controller.CreateReservationRequest;
import com.shionsuio.hotel.exception.RoomNotFoundException;
import com.shionsuio.hotel.exception.ReservationConflictException;
import com.shionsuio.hotel.repository.ReservationRepository;
import com.shionsuio.hotel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }


    //ロック処理
    @Transactional
    public Long create(CreateReservationRequest request) {

        try {
            roomRepository.findByIdForUpdate(request.roomId());
        } catch (EmptyResultDataAccessException exception) {
            log.info("予約対象の部屋が存在しません roomId={}", request.roomId());
            throw new RoomNotFoundException(request.roomId());
        }


        boolean roomIsAvailable = roomRepository.findAvailableRoom(request.checkInDate(),request.checkOutDate())
                .stream().anyMatch(
                        room -> room.Id().equals(request.roomId())
                );

        if (!roomIsAvailable) {
            log.info("予約競合 roomId={} checkIn={} checkOut={}",
                    request.roomId(), request.checkInDate(), request.checkOutDate());
            throw new ReservationConflictException("指定期間は予約できません");
        }

        Long reservationId = reservationRepository.save(
                request.roomId(),
                request.checkInDate(),
                request.checkOutDate()
        );

        log.info("予約を作成しました reservationId={} roomId={}",
                reservationId, request.roomId());
        return reservationId;
    }


}
