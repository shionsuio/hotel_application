curl -u demo-user:demo-password \
-X POST http://localhost:8080/reservations \
-H "Content-Type: application/json" \
-H "Idempotency-Key: manual-test-001" \
-d '{
"roomId": 1,
"checkInDate": "2099-02-10",
"checkOutDate": "2099-02-12"
}'

エントリポイントから立ち上げてターミナルで実行
１回目は成功した時のログが出ます
同じ内容で２回、同じIdempotency-keyが同じで違うリクエストを送る場合は例外処理になります。