package com.shionsuio.hotel.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    // TODO: ReservationServiceと予約用Repositoryを作成し、予約をDBへ保存して予約IDを返す。
    // TODO: Serviceの同一トランザクション内で部屋をロックし、期間重複を再確認してから予約を保存する。
    // TODO: ロック対象・取得順序・タイムアウトを決め、同じ部屋への同時予約を防ぐ。
    // TODO: Idempotency-Keyを受け取り、利用者とキーの組にDBの一意制約を設ける。
    // TODO: 冪等性の記録と予約を同一トランザクションで保存し、再送時は同じ結果を返す。同じキーで内容が違う場合は拒否する。
    // TODO: Security設定で予約APIの認証を必須にし、予約者を認証情報から取得する。
    // TODO: 入力不正・部屋なし・予約競合を400・404・409へ変換する共通例外処理を用意する。

    @PostMapping
    //POST /reservationsになる
    public CreateReservationRequest crateResponse(
            @RequestBody CreateReservationRequest request
            //リクエストの形式は別で定義しておく。@RequestBodyはJsonに変換してくれるもの
    ) {
        return null;
    }
}
