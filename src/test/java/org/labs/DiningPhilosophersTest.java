package org.labs;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiningPhilosophersTest {

    @Test
    void lunchCompletesWithoutDeadlockAndFoodIsDistributedFairly() throws Exception {
        int programmerCount = 7;
        int spoonCount = programmerCount;
        int soupCount = 1000;
        int waiterCount = 2;

        Restaurant restaurant = new Restaurant(waiterCount, soupCount, 0);
        SpoonManager spoonManager = new SpoonManager(spoonCount);
        List<Programmer> programmers = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(programmerCount);
        try {
            for (int i = 0; i < programmerCount; i++) {
                Programmer programmer = new Programmer(i, restaurant, spoonManager, spoonCount);
                programmers.add(programmer);
                executor.submit(programmer);
            }

            executor.shutdown();
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS),
                    "Обед должен завершиться без взаимной блокировки");

            int totalEaten = programmers.stream().mapToInt(Programmer::getNumberOfEaten).sum();
            assertEquals(soupCount, totalEaten,
                    "Все порции должны быть съедены ровно по одному разу");

            int average = soupCount / programmerCount;
            for (Programmer programmer : programmers) {
                assertTrue(programmer.getNumberOfEaten() > 0,
                        "Каждый программист должен поесть хотя бы один раз");
                assertTrue(programmer.getNumberOfEaten() <= average * 2,
                        "Еда должна распределяться примерно поровну");
            }
        } finally {
            executor.shutdownNow();
            restaurant.shutdown();
        }
    }
}