package org.example.CPUBound;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class PiCalculator {
    public static int insidePointsGlobal = 0;
    private final static SecureRandom random = new SecureRandom();

    final static int TOTAL_POINTS = 1_000_000_000;
    private static final List<Integer> THREADS = List.of(1,6,12);

    public static void main(String[] args) {
        for(int threads: THREADS) {
            long startTime = System.currentTimeMillis();
            System.out.println(PiCalculator.calculate(TOTAL_POINTS, threads));
            System.out.println("Threads: " + threads);
            System.out.println("Час виконання: " + (System.currentTimeMillis() - startTime) + " мс");
        }
    }

    public static double calculate(int totalPoints, int numberOfThreads) {
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
