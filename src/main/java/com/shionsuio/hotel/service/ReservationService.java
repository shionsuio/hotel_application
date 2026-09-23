package com.shionsuio.hotel.service;

import com.shionsuio.hotel.controller.CreateReservationRequest;
import com.shionsuio.hotel.exception.RoomNotFoundException;
import com.shionsuio.hotel.exception.ReservationNotFoundException;
import com.shionsuio.hotel.exception.IdempotencyConflictException;
import com.shionsuio.hotel.exception.ReservationConflictException;
import com.shionsuio.hotel.domain.IdempotencyRecord;
import com.shionsuio.hotel.repository.IdempotencyRepository;
import com.shionsuio.hotel.repository.ReservationRepository;
import com.shionsuio.hotel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final IdempotencyRepository idempotencyRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository,
                              IdempotencyRepository idempotencyRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.idempotencyRepository = idempotencyRepository;
    }


    //ロック処理
    @Transactional
    public Long create(CreateReservationRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IdempotencyConflictException("Idempotency-Keyは必須です");
        }

        String requestHash = hashRequest(request);
        IdempotencyRecord existing = idempotencyRepository.findByKey(idempotencyKey);
        if (existing != null) {
            return resolveExisting(existing, requestHash);
        }

        if (idempotencyRepository.insertIfAbsent(idempotencyKey, requestHash) == 0) {
            existing = idempotencyRepository.findByKey(idempotencyKey);
            return resolveExisting(existing, requestHash);
        }

        try {
            roomRepository.findByIdForUpdate(request.roomId());
        } catch (EmptyResultDataAccessException exception) {
            log.info("予約対象の部屋が存在しません roomId={}", request.roomId());
            throw new RoomNotFoundException(request.roomId());
        }


        boolean roomIsAvailable = roomRepository.findAvailableRoom(request.checkInDate(),request.checkOutDate())
                .stream().anyMatch(
                        room -> room.id().equals(request.roomId())
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
        idempotencyRepository.attachReservation(idempotencyKey, reservationId);

        log.info("予約を作成しました reservationId={} roomId={}",
                reservationId, request.roomId());
        return reservationId;
    }

    @Transactional
    public void cancel(Long reservationId) {
        int updatedRows = reservationRepository.cancel(reservationId);
        if (updatedRows == 0) {
            throw new ReservationNotFoundException(reservationId);
        }

        log.info("予約をキャンセルしました reservationId={}", reservationId);
    }

    private Long resolveExisting(IdempotencyRecord existing, String requestHash) {
        if (existing == null || !existing.requestHash().equals(requestHash)) {
            throw new IdempotencyConflictException("同じIdempotency-Keyで異なる内容は送信できません");
        }
        if (existing.reservationId() == null) {
            throw new IdempotencyConflictException("同じIdempotency-Keyの処理が実行中です");
        }
        return existing.reservationId();
    }


    //冪等性確保のためのハッシュ関数
    private String hashRequest(CreateReservationRequest request) {

        //ここの入力が同じものならハッシュ値が同じになります
        String value = request.roomId() + "|" + request.checkInDate() + "|" + request.checkOutDate();
        try {
            //扱いやすいように１６進数に変換している
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("リクエストハッシュを作成できません", exception);
        }
    }


}
