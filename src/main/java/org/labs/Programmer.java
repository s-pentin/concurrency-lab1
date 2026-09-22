package org.labs;

import java.util.concurrent.ExecutionException;

public class Programmer implements Runnable {

    private final int id;
    private int numberOfEaten;
    private final SpoonManager spoonManager;
    private final Restaurant restaurant;
    private final int spoonCount;

    public Programmer(int id, Restaurant restaurant, SpoonManager spoonManager, int spoonCount) {
        this.id = id;
        this.numberOfEaten = 0;
        this.restaurant = restaurant;
        this.spoonManager = spoonManager;
        this.spoonCount = spoonCount;
    }

    @Override
    public void run() {
        while (true) {
            talk();
            int leftSpoonId = (id - 1 + spoonCount) % spoonCount;
            if (!askForSoup()) {
                break;
            }
            try {
                spoonManager.acquire(leftSpoonId, id);
                try {
                    eat();
                } finally {
                    spoonManager.release(leftSpoonId, id);
                }
            } catch (InterruptedException e) {
                System.out.println("Программист " + this.id + " был прерван во время ожидания ложек");
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("Программист " + this.id + " закончил обед. " + "Съедено порций: " + numberOfEaten);
    }

    public void eat() {
        System.out.println(this.id + " начал есть");
        numberOfEaten++;
    }

    public void talk() {
        System.out.println(this.id + " начал разговаривать о лучших преподавалетях");
    }

    public int getNumberOfEaten() {
        return numberOfEaten;
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
