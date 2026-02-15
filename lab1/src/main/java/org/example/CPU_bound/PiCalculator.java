package org.example.CPU_bound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class PiCalculator {
    public static int insidePointsGlobal = 0;

    public static double calculate(int totalPoints) {
        int insidePoints = getInsidePoints(totalPoints);
        return 4.0 * insidePoints / totalPoints;
    }

    public static double calculateInParallel(int totalPoints, int numberOfThreads) {
        int totalPointsForThread = totalPoints / numberOfThreads;
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

    static class Runner implements Runnable {
        int totalPoints;

        public Runner(int totalPoints) {
            this.totalPoints = totalPoints;
        }

        @Override
        public void run() {
            PiCalculator.addInsidePoints(PiCalculator.getInsidePoints(totalPoints));
        }
    }

    synchronized static void addInsidePoints(int points) {
        insidePointsGlobal += points;
    }

    public static int getInsidePoints(int totalPoints) {
        BiPredicate<Double, Double> pred = (x, y) -> (x * x + y * y) <= 1;
        int insidePoints = 0;
        for (int i = 0; i < totalPoints; i++) {
            Point p = Point.generate();
            if (pred.test(p.x(), p.y())) {
                insidePoints++;
            }
        }
        return insidePoints;
    }

    record Point(double x, double y) {

        static Point generate() {
            double x = Math.random();
            double y = Math.random();
            return new Point(x, y);
        }
    }
}
