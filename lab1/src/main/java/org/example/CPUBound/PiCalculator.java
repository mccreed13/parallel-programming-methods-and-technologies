package org.example.CPUBound;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;

public class PiCalculator {
    public static int insidePointsGlobal = 0;

    public static double calculate(int totalPoints) {
        int insidePoints = getInsidePoints(totalPoints);
        return 4.0 * insidePoints / totalPoints;
    }

    public static double calculateInParallel(int totalPoints, int numberOfThreads) {
        int totalPointsForThread = totalPoints / numberOfThreads;
        System.out.println("Total points for thread: " + totalPointsForThread);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            Thread t = new Thread(new Runner(totalPointsForThread));
            threads.add(t);
        }
        threads.forEach(Thread::start);
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return 4.0 * insidePointsGlobal / totalPoints;
    }

    synchronized static void addInsidePoints(int points) {
        insidePointsGlobal += points;
    }

    public static int getInsidePoints(int totalPoints) {
        BiPredicate<Double, Double> isInCircle = (x, y) -> (x * x + y * y) <= 1;
        int insidePoints = 0;
        Random random = new Random();
        for (int i = 0; i < totalPoints; i++) {
            if (isInCircle.test(random.nextDouble(), random.nextDouble())) {
                insidePoints++;
            }
        }
        return insidePoints;
    }
}

class Runner implements Runnable {
    int totalPoints;

    public Runner(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    @Override
    public void run() {
        PiCalculator.addInsidePoints(PiCalculator.getInsidePoints(totalPoints));
    }
}
