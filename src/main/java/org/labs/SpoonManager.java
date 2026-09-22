package org.labs;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SpoonManager {
    private final ReentrantLock reentrantLock = new ReentrantLock(true);
    private final Condition spoonsAvailable = reentrantLock.newCondition();
    private final boolean[] usedSpoons;

    public SpoonManager(int spoonCount) {
        this.usedSpoons = new boolean[spoonCount];
    }

    public void release(int leftSpoon, int rightSpoon) {
        reentrantLock.lock();
        try {
            usedSpoons[leftSpoon] = false;
            usedSpoons[rightSpoon] = false;
            spoonsAvailable.signalAll();
        } finally {
            reentrantLock.unlock();
        }
    }

    public void acquire(int leftSpoon, int rightSpoon) throws InterruptedException {
        reentrantLock.lock();
        try {
            while (!tryAcquireWithoutLock(leftSpoon, rightSpoon)) {
                spoonsAvailable.await();
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    private boolean tryAcquireWithoutLock(int leftSpoon, int rightSpoon) {
        if (!usedSpoons[leftSpoon] && !usedSpoons[rightSpoon]) {
            usedSpoons[leftSpoon] = true;
            usedSpoons[rightSpoon] = true;
            return true;
        } else {
            return false;
        }
    }
}
