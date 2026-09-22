package org.labs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Restaurant {

    private final AtomicBoolean lunchFinished = new AtomicBoolean(false);
    private final ExecutorService waiters;
    private final AtomicInteger soupCount;
    private final int waiterDelayMillis;

    public Restaurant(int countWaiters, int soupCount, int waiterDelayMillis) {
        this.waiters = Executors.newFixedThreadPool(countWaiters);
        this.soupCount = new AtomicInteger(soupCount);
        this.waiterDelayMillis = waiterDelayMillis;
    }

    public boolean takeSoup() {
        while (!lunchFinished.get()) {
            int current = soupCount.get();

            if (current <= 0) {
                lunchFinished.set(true);
                return false;
            }

            if (soupCount.compareAndSet(current, current - 1)) {
                if (current == 1) {
                    lunchFinished.set(true);
                }
                return true;
            }
        }
        return false;
    }

    public SoupRequest requestSoup(int programmerId) {
        SoupRequest request = new SoupRequest(programmerId);

        waiters.submit(() -> {
            try {
                Thread.sleep(waiterDelayMillis);

                if (takeSoup()) {
                    request.complete(SoupResult.SOUP_RECEIVED);
                } else {
                    System.out.println("Заказы больше не принимаются / суп закончился");
                    request.complete(SoupResult.LUNCH_FINISHED);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                request.getResult().completeExceptionally(e);
            }
        });

        return request;
    }

    public void shutdown() {
        waiters.shutdown();
    }
}
