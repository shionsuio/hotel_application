package com.shionsuio.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HotelApplicationTests {

    // TODO: 空室検索の期間重複・チェックアウト当日の次のチェックイン・キャンセル済み予約の扱いをテストする。
    // TODO: 同じ部屋・重なる期間への並行予約で、成功が1件だけになることを実DBの統合テストで確認する。
    // TODO: 同じ冪等性キーの逐次再送・同時送信で予約が1件になり、同じ結果が返ることを確認する。
    // TODO: 同じキーで異なる内容を送った場合の拒否と、異なる利用者間でのキーの独立性を確認する。
    // TODO: 予約保存途中の失敗で、予約と冪等性の記録が両方ロールバックされることを確認する。
    // TODO: 未認証アクセス・不正入力の拒否をテストし、予約参照・変更API追加時は他人の予約へのアクセス拒否も確認する。
    // TODO: ロックの検証には採用する本番DBと同じ種類のDBを使い、H2だけで判断しない。

    @Test
    void contextLoads() {
    }

}
