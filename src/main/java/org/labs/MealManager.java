package org.labs;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MealManager {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition mealAvailable = lock.newCondition();

    private final int[] eatenCounts;

    private int minEatenCount;
    private int programmersAtMin;
    private int activeCount;

    public MealManager(int programmerCount) {
        this.eatenCounts = new int[programmerCount];
        this.minEatenCount = 0;
        this.programmersAtMin = programmerCount;
        this.activeCount = programmerCount;
    }

    public void waitForTurn(int programmerId) throws InterruptedException {
        lock.lock();
        try {
            while (eatenCounts[programmerId] > minEatenCount) {
                mealAvailable.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void finishedEating(int programmerId) {
        lock.lock();
        try {
            if (eatenCounts[programmerId] != minEatenCount) {
                throw new IllegalStateException("Программист " + programmerId + " съел без очереди");
            }

            eatenCounts[programmerId]++;
            programmersAtMin--;

            if (programmersAtMin == 0) {
                minEatenCount++;
                programmersAtMin = activeCount;

                mealAvailable.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void leave(int programmerId) {
        lock.lock();
        try {
            if (eatenCounts[programmerId] != minEatenCount) {
                throw new IllegalStateException("Программист " + programmerId + " вышел без очереди");
            }

            activeCount--;
            programmersAtMin--;

            if (programmersAtMin == 0 && activeCount > 0) {
                minEatenCount++;
                programmersAtMin = activeCount;

                mealAvailable.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public int getEatenCount(int programmerId) {
        lock.lock();
        try {
            return eatenCounts[programmerId];
        } finally {
            lock.unlock();
        }
    }
}