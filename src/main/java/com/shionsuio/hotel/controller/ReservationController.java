package com.shionsuio.hotel.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @PostMapping
    //POST /reservationsになる
    public CreateReservationRequest crateResponse(
            @RequestBody CreateReservationRequest request
            //リクエストの形式は別で定義しておく。@RequestBodyはJsonに変換してくれるもの
    ) {
        return null;
    }
}
