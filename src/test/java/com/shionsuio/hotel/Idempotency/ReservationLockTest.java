package com.shionsuio.hotel.Idempotency;

import com.shionsuio.hotel.controller.CreateReservationRequest;
import com.shionsuio.hotel.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ReservationLockTest {

    private final ReservationService reservationService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationLockTest(ReservationService reservationService,
                               JdbcTemplate jdbcTemplate) {
        this.reservationService = reservationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    //２回目以降のテスト実行で既存の予約を再利用するため、成功扱いになってしまう可能性があるため実行前後で削除している
    @BeforeEach
    void cleanUpBeforeTest() {
        cleanUp();
    }

    @AfterEach
    void cleanUpAfterTest() {
        cleanUp();
    }

    private void cleanUp() {
        jdbcTemplate.update("""
                DELETE FROM idempotency_keys
                WHERE idempotency_key LIKE 'concurrent-%'
                """);
        jdbcTemplate.update("""
                DELETE FROM reservations
                WHERE room_id = 1
                  AND check_in_date = DATE '2099-07-10'
                  AND check_out_date = DATE '2099-07-12'
                """);
    }

    private CreateReservationRequest request() {
        return new CreateReservationRequest(
                1L,
                LocalDate.of(2099, 7, 10),
                LocalDate.of(2099, 7, 12)
        );
    }

    @Test
    @DisplayName("同時に予約が入った場合は片方だけ成功する")
    void testCurrentLock() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            for (int i = 0; i < numberOfThreads; i++) {
                int taskIndex = i;

                executor.submit(() -> {
                    readyLatch.countDown();

                    try {
                        startLatch.await();
                        reservationService.create(
                                request(),
                                "concurrent-" + taskIndex
                        );
                        successCount.incrementAndGet();
                    } catch (Exception exception) {
                        failureCount.incrementAndGet();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            readyLatch.await();
            startLatch.countDown();

            boolean completed = finishLatch.await(5, TimeUnit.SECONDS);

            assertThat(completed).isTrue();
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failureCount.get()).isEqualTo(1);

            Integer reservationCount = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM reservations
                    WHERE room_id = 1
                      AND check_in_date = DATE '2099-07-10'
                      AND check_out_date = DATE '2099-07-12'
                    """, Integer.class);

            assertThat(reservationCount).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
