package org.example.patterns;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class MinMaxAverageMedian {
    private final static int ARRAY_SIZE = 1_000_000;
    private final static int MIN = 10;
    private final static int MAX = 100;
    private final static int[] ARRAY = generateArray(ARRAY_SIZE, MIN, MAX);

    public static int[] generateArray(int size, int min, int max) {
        Random random = new Random();
        return IntStream.range(0, size)
                .map(i -> random.nextInt(min, max + 1))
                .toArray();
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        parallel();
        System.out.println("Parallel: " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        sequential();
        System.out.println("Sequential: " + (System.currentTimeMillis() - start) + "ms");
    }

    private static void sequential() {
        int[] sortedData = ARRAY.clone();
        // O(N logN)
        Arrays.sort(sortedData); // Використовує Fork-Join
        System.out.println("Median: " + getMedian(sortedData));
        System.out.println("Min: " + getMin(sortedData));
        System.out.println("Max: " + getMax(sortedData));
        System.out.println("Average: " + getSequentialAverage());
    }

    private static double getSequentialAverage() {
        BigDecimal bd = new BigDecimal(BigInteger.ZERO);
        for (int i = 0; i < ARRAY_SIZE; i++) {
            bd = bd.add(BigDecimal.valueOf(ARRAY[i]));
        }
        return bd.divide(BigDecimal.valueOf(ARRAY_SIZE), 2, RoundingMode.HALF_UP).doubleValue();
    }

    private static void parallel() {
        int[] sortedData = ARRAY.clone();
        // O(N logN)
        Arrays.parallelSort(sortedData); // Використовує Fork-Join
        System.out.println("Median: " + getMedian(sortedData));
        System.out.println("Min: " + getMin(sortedData));
        System.out.println("Max: " + getMax(sortedData));
        System.out.println("Average: " + getParallelAverage());
    }

    private static double getParallelAverage() {
        return getSequentialAverage();
    }

    private static int getMax(int[] data) {
        return data[ARRAY_SIZE - 1];
    }

    private static int getMin(int[] data) {
        return data[0];
    }

    private static double getMedian(int[] data) {
        double median;
        if (ARRAY_SIZE % 2 == 0) {
            median = (data[ARRAY_SIZE / 2] + data[ARRAY_SIZE / 2 - 1]) / 2.0;
        } else {
            median = data[ARRAY_SIZE / 2];
        }
        return median;
    }
}
