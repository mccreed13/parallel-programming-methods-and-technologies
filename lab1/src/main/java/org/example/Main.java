package org.example;

import org.example.CPUBound.PiCalculator;

public class Main {
    final static int TOTAL_POINTS = 1_000_000_000;
    final static int NUMBER_OF_THREADS = 2;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println(PiCalculator.calculate(TOTAL_POINTS, NUMBER_OF_THREADS));
        System.out.println("Час паралельного виконання: " + (System.currentTimeMillis() - startTime) + " мс");

        startTime = System.currentTimeMillis();
        System.out.println(PiCalculator.calculate(TOTAL_POINTS));
        System.out.println("Час послідовного виконання: " + (System.currentTimeMillis() - startTime) + " мс");
    }
}