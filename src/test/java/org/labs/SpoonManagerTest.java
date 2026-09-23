package org.labs;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpoonManagerTest {

    @Test
    void acquireAndReleaseAllowsReuse() throws InterruptedException {
        SpoonManager manager = new SpoonManager(2);

        manager.acquire(0, 1);
        manager.release(0, 1);

        assertDoesNotThrow(() -> manager.acquire(0, 1));
        manager.release(0, 1);
    }

    @Test
    void acquireBlocksWhileSpoonsAreInUse() throws Exception {
        SpoonManager manager = new SpoonManager(2);
        manager.acquire(0, 1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> blocked = executor.submit(() -> {
                manager.acquire(0, 1);
                manager.release(0, 1);
            });

            assertThrows(TimeoutException.class, () -> blocked.get(200, TimeUnit.MILLISECONDS));

            manager.release(0, 1);

            blocked.get(1, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void disjointSpoonsCanBeAcquiredConcurrently() throws Exception {
        SpoonManager manager = new SpoonManager(4);
        manager.acquire(0, 1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> other = executor.submit(() -> {
                manager.acquire(2, 3);
            });

            // Spoons 2 and 3 are free, so acquisition should not block.
            other.get(1, TimeUnit.SECONDS);
        } finally {
            manager.release(0, 1);
            executor.shutdownNow();
        }
    }

    @Test
    void onlyOneThreadHoldsASpoonAtATime() throws Exception {
        int threads = 8;
        SpoonManager manager = new SpoonManager(2);
        AtomicInteger currentHolders = new AtomicInteger();
        AtomicInteger maxHolders = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    try {
                        start.await();
                        manager.acquire(0, 1);
                        int now = currentHolders.incrementAndGet();
                        maxHolders.accumulateAndGet(now, Math::max);
                        Thread.sleep(1);
                        currentHolders.decrementAndGet();
                        manager.release(0, 1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
            }

            start.countDown();
            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            assertEquals(1, maxHolders.get(), "Две нити не должны одновременно владеть одной и той же ложкой");
        } finally {
            pool.shutdownNow();
        }
    }
}