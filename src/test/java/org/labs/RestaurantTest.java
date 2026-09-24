package org.labs;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestaurantTest {

    @Test
    void takeSoupReturnsTrueExactlySoupCountTimes() {
        int soupCount = 5;
        Restaurant restaurant = new Restaurant(1, soupCount, 0);

        for (int i = 0; i < soupCount; i++) {
            assertTrue(restaurant.takeSoup());
        }
        assertFalse(restaurant.takeSoup());
    }

    @Test
    void takeSoupIsThreadSafeAndNeverExceedsCount() throws Exception {
        int totalSoup = 10_000;
        int threads = 8;
        Restaurant restaurant = new Restaurant(1, totalSoup, 0);
        AtomicInteger taken = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    while (restaurant.takeSoup()) {
                        taken.incrementAndGet();
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            assertEquals(totalSoup, taken.get(), "Каждая порция должна быть выдана ровно один раз");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void requestSoupCompletesWithSoupReceived() throws Exception {
        Restaurant restaurant = new Restaurant(1, 10, 0);
        try {
            SoupRequest request = restaurant.requestSoup(42);

            assertEquals(42, request.getProgrammerId());
            assertEquals(SoupResult.SOUP_RECEIVED, request.getResult().get(1, TimeUnit.SECONDS));
        } finally {
            restaurant.shutdown();
        }
    }

    @Test
    void requestSoupCompletesWithLunchFinishedWhenSoupIsOut() throws Exception {
        Restaurant restaurant = new Restaurant(1, 1, 0);
        try {
            SoupRequest first = restaurant.requestSoup(1);
            assertEquals(SoupResult.SOUP_RECEIVED, first.getResult().get(1, TimeUnit.SECONDS));

            SoupRequest second = restaurant.requestSoup(2);
            assertEquals(SoupResult.LUNCH_FINISHED, second.getResult().get(1, TimeUnit.SECONDS));
        } finally {
            restaurant.shutdown();
        }
    }
}