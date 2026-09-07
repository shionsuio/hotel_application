package com.shionsuio.hotel.repository;

import com.shionsuio.hotel.domain.Room;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomRepository {
    // TODO: JDBC実装とrooms・reservationsのDBマイグレーションを追加する。
    // TODO: 既存予約の開始 < 検索終了 AND 既存予約の終了 > 検索開始を重複とし、キャンセル済み予約は除外する。
    // TODO: SQLはプレースホルダーで値を渡し、検索条件を文字列連結しない。
    // TODO: 予約件数を増やして実行計画と応答時間を測定し、部屋ID・宿泊期間などのインデックスを検討する。
    List<Room> findAvailableRoom(
            LocalDateTime checkIn,
            LocalDateTime checkOut
    );
}
