package org.labs;

import java.util.concurrent.locks.ReentrantLock;

public class SpoonManager {
    private final ReentrantLock[] reentrantLocksSpoon;

    public SpoonManager(int spoonCount) {
        reentrantLocksSpoon = new ReentrantLock[spoonCount];
        for (int i = 0; i < spoonCount; i++) {
            this.reentrantLocksSpoon[i] = new ReentrantLock(true);
        }
    }

    public void acquire(int leftSpoon, int rightSpoon) {
        int first = Math.min(leftSpoon, rightSpoon);
        int second = Math.max(leftSpoon, rightSpoon);
        reentrantLocksSpoon[first].lock();
        reentrantLocksSpoon[second].lock();
    }

    public void release(int leftSpoon, int rightSpoon) {
        int first = Math.min(leftSpoon, rightSpoon);
        int second = Math.max(leftSpoon, rightSpoon);
        reentrantLocksSpoon[second].unlock();
        reentrantLocksSpoon[first].unlock();
    }
}
