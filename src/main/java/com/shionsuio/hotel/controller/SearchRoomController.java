package com.shionsuio.hotel.controller;

import com.shionsuio.hotel.service.SearchRoomService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")

public class SearchRoomController {

    private final SearchRoomService searchRoomService;

    public SearchRoomController(SearchRoomService searchRoomService) {
        this.searchRoomService = searchRoomService;
    }

    @PostMapping
    public List<SearchRoomsResponse> SearchRoom(
            @RequestBody CreateSearchRequest request
    )
    {
        return searchRoomService.search(request);
    }
}
