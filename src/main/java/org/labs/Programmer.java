package org.labs;

import java.util.concurrent.ExecutionException;

public class Programmer implements Runnable {

    private final int id;
    private final SpoonManager spoonManager;
    private final Restaurant restaurant;
    private final int spoonCount;
    private final MealManager mealManager;

    public Programmer(int id, Restaurant restaurant, SpoonManager spoonManager, MealManager mealManager, int spoonCount) {
        this.id = id;
        this.restaurant = restaurant;
        this.spoonManager = spoonManager;
        this.mealManager = mealManager;
        this.spoonCount = spoonCount;
    }

    @Override
    public void run() {
        while (true) {
            talk();
            try {
                mealManager.waitForTurn(id);

                if (!askForSoup()) {
                    mealManager.leave(id);
                    break;
                }

                int leftSpoonId = (id - 1 + spoonCount) % spoonCount;
                spoonManager.acquire(leftSpoonId, id);

                try {
                    eat();
                    mealManager.finishedEating(id);
                } finally {
                    spoonManager.release(leftSpoonId, id);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("Программист " + this.id + " закончил обед. " + "Съедено порций: " + mealManager.getEatenCount(id));
    }

    public void eat() {
        System.out.println(this.id + " начал есть");
    }

    public void talk() {
        System.out.println(this.id + " начал разговаривать о лучших преподавалетях");
    }

    private boolean askForSoup() {
        try {
            SoupRequest request = restaurant.requestSoup(this.id);
            return request.getResult().get().equals(SoupResult.SOUP_RECEIVED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Для программиста с id=" + this.id + " - произошла ошибка с запросом супа");
            return false;
        } catch (ExecutionException e) {
            System.out.println("Для программиста с id=" + this.id + ": " + e.getCause());
            return false;
        }
    }
}
