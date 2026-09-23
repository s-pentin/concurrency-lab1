package org.labs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Restaurant {

    private final ExecutorService waiters;
    private final Semaphore semaphore;
    private final AtomicInteger soupCount;
    private final int waiterDelayMillis;

    public Restaurant(int countWaiters, int soupCount, int waiterDelayMillis) {
        this.waiters = Executors.newVirtualThreadPerTaskExecutor();
        this.semaphore = new Semaphore(countWaiters, true);
        this.soupCount = new AtomicInteger(soupCount);
        this.waiterDelayMillis = waiterDelayMillis;
    }

    public SoupRequest requestSoup(int programmerId) {
        SoupRequest request = new SoupRequest(programmerId);

        waiters.submit(() -> {
            boolean acquired = false;

            try {
                semaphore.acquire();
                acquired = true;

                Thread.sleep(waiterDelayMillis);

                if (takeSoup()) {
                    request.complete(SoupResult.SOUP_RECEIVED);
                } else {
                    request.complete(SoupResult.LUNCH_FINISHED);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                request.getResult().completeExceptionally(e);

            } finally {
                if (acquired) {
                    semaphore.release();
                }
            }
        });

        return request;
    }

    public void shutdown() {
        waiters.shutdown();
    }

    boolean takeSoup() {
        int previous = soupCount.getAndUpdate(count -> count > 0 ? count - 1 : 0);
        return previous > 0;
    }
}
