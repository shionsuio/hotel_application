package com.shionsuio.hotel.service;

import com.shionsuio.hotel.controller.CreateReservationRequest;
import com.shionsuio.hotel.repository.ReservationRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Long create(CreateReservationRequest request) {
        return reservationRepository.save(
                request.roomId(),
                request.checkInDate(),
                request.checkOutDate()
        );
    }


}

