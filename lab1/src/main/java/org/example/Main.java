package org.example;

import org.example.CPUBound.PiCalculator;

public class Main {
    final static int TOTAL_POINTS = 1_000_000_000;
    final static int NUMBER_OF_THREADS = 2;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println(PiCalculator.calculate(TOTAL_POINTS));
        long endTime = System.currentTimeMillis();
        System.out.println("Sequence time = " + (endTime-startTime));

        startTime = System.currentTimeMillis();
        System.out.println(PiCalculator.calculateInParallel(TOTAL_POINTS, NUMBER_OF_THREADS));
        endTime = System.currentTimeMillis();
        System.out.println("Parallel time = " + (endTime-startTime));
    }
}