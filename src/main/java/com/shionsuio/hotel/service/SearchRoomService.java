package com.shionsuio.hotel.service;


import com.shionsuio.hotel.controller.CreateSearchRequest;
import com.shionsuio.hotel.controller.SearchRoomsResponse;
import com.shionsuio.hotel.domain.Room;
import com.shionsuio.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchRoomService {

    private final RoomRepository roomRepository;
    public SearchRoomService(
            RoomRepository roomRepository
    ) {
        this.roomRepository = roomRepository;
    }

    public  List<SearchRoomsResponse> search(
            CreateSearchRequest request
    ) {

        // 検索結果は予約確定を保証しないため、予約Service側でロック取得後に空室を再確認する。

        List<Room> rooms = roomRepository.findAvailableRoom(
                request.checkInDate(), request.checkOutDate()
        );
        return rooms.stream()
                .map(room -> new SearchRoomsResponse(
                        room.id(),
                        room.roomNumber(),
                        room.price()
                )).toList();
    }
}
