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

        // TODO: 検索結果の上限・ページングを設け、大量データの全件取得を避ける。
        // TODO: 検索時点の空室は予約確定を保証しないため、予約Service側でもロック取得後に空室を再確認する。

        List<Room> rooms = roomRepository.findAvailableRoom(
                request.checkInTime(), request.checkOutTime()
        );
        return rooms.stream()
                .map(room -> new SearchRoomsResponse(
                        room.Id(),
                        room.roomNumber(),
                        room.price()
                )).toList();
        //recordでrequest形式を定義すると自動でゲッターメソッドができる。だからcheckInTime()は定義していない
    }
}
