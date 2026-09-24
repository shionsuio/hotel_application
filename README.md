# Hotel Reservation API

ホテルの空室検索・予約・キャンセルを扱うSpring Boot APIです。

## 技術スタック

- Java 17
- Spring Boot
- Spring JDBC (`JdbcTemplate`)
- Spring Security (ローカル用HTTP Basic)
- PostgreSQL 17
- Docker Compose

## 起動方法

PostgreSQLを起動します。

```bash
docker compose up -d db
```

アプリを起動します。

```bash
./gradlew bootRun
```

デフォルトの認証情報は以下です。環境変数で変更できます。

```text
username: demo-user
password: demo-password
```

```bash
export HOTEL_SECURITY_USERNAME=demo-user
export HOTEL_SECURITY_PASSWORD=demo-password
```

## API

### 空室検索

```bash
curl -u demo-user:demo-password \
  -X POST http://localhost:8080/search \
  -H 'Content-Type: application/json' \
  -d '{
    "checkInDate": "2099-10-10",
    "checkOutDate": "2099-10-12"
  }'
```

### 予約作成

`Idempotency-Key`は、同じ予約操作を再送するときに同じ値を使用します。

```bash
curl -u demo-user:demo-password \
  -X POST http://localhost:8080/reservations \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: example-reservation-001' \
  -d '{
    "roomId": 1,
    "checkInDate": "2099-10-10",
    "checkOutDate": "2099-10-12"
  }'
```

同じキー・同じ内容の再送では、同じ予約IDを返します。同じキーで内容が異なる場合は`409 Conflict`です。

### 予約キャンセル

```bash
curl -u demo-user:demo-password \
  -X DELETE http://localhost:8080/reservations/1
```

成功時は`204 No Content`を返し、予約のステータスを`CANCELED`に変更します。

## 主な仕様

- `@Valid`でリクエストを検証し、不正な入力は`400`で返す
- 予約作成全体を`@Transactional`で管理する
- 対象部屋を`SELECT ... FOR UPDATE`でロックしてから空室を再確認する
- `Idempotency-Key`とリクエストハッシュで二重送信を防ぐ
- 予約競合は`409`、存在しない部屋・予約は`404`で返す
- Controller、Service、Repositoryを分離する

## テスト

```bash
./gradlew test
```

主なテスト内容は、入力検証、HTTPレスポンス、冪等性、リクエストハッシュ、PostgreSQL上の同時予約ロックです。

## 未対応

- 予約と認証ユーザーの紐付け、所有者認可
- FlywayなどによるDBマイグレーション
- JWT/OIDCなど本番向け認証
- ロック待ちタイムアウトと負荷測定
- 本番環境のTLS、Secret管理、監視

## ドキュメント

- [機能要件](FUNCTIONAL_REQUIREMENTS.md)
