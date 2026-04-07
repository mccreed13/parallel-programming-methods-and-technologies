package org.example.IPC;

import java.util.concurrent.atomic.AtomicInteger;

public class SharedMemory {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger sharedData = new AtomicInteger();

        Thread worker = new Thread(() -> {
            while (sharedData.get() <= 0) Thread.onSpinWait(); // Очікування
            System.out.println("Worker: Отримано число " + sharedData.get());
            sharedData.set(sharedData.get() * -1); // Повернення результату
        });

        worker.start();
        int randomNum = (int) (Math.random() * 100);
        System.out.println("Main: Згенеровано " + randomNum);
        sharedData.set(randomNum);

        worker.join();
        System.out.println("Main: Отримано назад " + sharedData.get());
    }
}
