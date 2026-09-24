package org.labs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int WAITER_COUNT = 2;
    private static final int PROGRAMMER_COUNT = 7;
    private static final int SPOON_COUNT = PROGRAMMER_COUNT;
    private static final int SOUP_COUNT = 1_000_000;
    private static final int WAITER_DELAY_MILLIS = 0;

    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant(WAITER_COUNT, SOUP_COUNT, WAITER_DELAY_MILLIS);
        SpoonManager spoonManager = new SpoonManager(SPOON_COUNT);
        MealManager mealManager = new MealManager(PROGRAMMER_COUNT);
        try (ExecutorService programmerExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < PROGRAMMER_COUNT; i++) {
                Programmer programmer = new Programmer(i, restaurant, spoonManager, mealManager, SPOON_COUNT);
                programmerExecutor.submit(programmer);
            }

            try {
                boolean terminated = programmerExecutor.awaitTermination(1, TimeUnit.MINUTES);
                if (!terminated) {
                    programmerExecutor.shutdownNow();

                    if (!programmerExecutor.awaitTermination(15, TimeUnit.SECONDS)) {
                        System.out.println("Не удалось корректно завершить programmerExecutor");
                    }
                }
            } catch (InterruptedException e) {
                programmerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                restaurant.shutdown();
            }
        }
    }
}