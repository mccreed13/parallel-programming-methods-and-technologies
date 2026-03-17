package org.example.patterns;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

public class MinMaxAverageMedian {
    private final static int ARRAY_SIZE = 10_000_000;
    private final static int MIN = 100;
    private final static int MAX = 10000;
    private final static int[] ARRAY = generateArray();
    private final static int THRESHOLD_FOR_MIN_MAX = 100_000;

    public static int[] generateArray() {
        Random random = new Random();
        return IntStream.range(0, ARRAY_SIZE)
                .map(i -> random.nextInt(MIN, MAX + 1))
                .toArray();
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        parallel();
        System.out.println("Parallel: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("---------------------------------------------------------");
        start = System.currentTimeMillis();
        sequential();
        System.out.println("Sequential: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("---------------------------------------------------------");
        start = System.currentTimeMillis();
        mixed();
        System.out.println("Mixed: " + (System.currentTimeMillis() - start) + "ms");
    }

    private static void mixed() {
        int[] sortedData = ARRAY.clone();
        double[] results = new MinMaxTask(0, ARRAY_SIZE).compute();
        System.out.println("Min: " + results[0]);
        System.out.println("Max: " + results[1]);
        try {
            System.out.println("Average: " + getAverageWorkerPool());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Arrays.sort(sortedData);
        System.out.println("Median: " + getMedian(sortedData));
    }

    private static void sequential() {
        int[] sortedData = ARRAY.clone();
        Arrays.sort(sortedData);
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
        try {
            System.out.println("Average: " + getAverageWorkerPool());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public static double getAverageWorkerPool() throws Exception {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        int chunkSize = ARRAY_SIZE / cores;
        List<Future<Double>> sums = new ArrayList<>();

        for (int i = 0; i < cores; i++) {
            final int start = i * chunkSize;
            final int end = (i == cores - 1) ? ARRAY_SIZE : (i + 1) * chunkSize;
            sums.add(executor.submit(() -> {
                double localSum = 0;
                for (int j = start; j < end; j++) localSum += ARRAY[j];
                return localSum;
            }));
        }

        double totalSum = 0;
        for (Future<Double> f : sums) totalSum += f.get();
        executor.shutdown();
        return totalSum / ARRAY_SIZE;
    }

    static class MinMaxTask extends RecursiveTask<double[]> {
        private final int start, end;

        MinMaxTask(int start, int end) { this.start = start; this.end = end; }

        @Override
        protected double[] compute() {
            if (end - start <= THRESHOLD_FOR_MIN_MAX) {
                double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                for (int i = start; i < end; i++) {
                    if (ARRAY[i] < min) min = ARRAY[i];
                    if (ARRAY[i] > max) max = ARRAY[i];
                }
                return new double[]{min, max};
            }
            int mid = (start + end) / 2;
            MinMaxTask left = new MinMaxTask(start, mid);
            MinMaxTask right = new MinMaxTask(mid, end);
            left.fork();
            double[] rightRes = right.compute();
            double[] leftRes = left.join();
            return new double[]{Math.min(leftRes[0], rightRes[0]), Math.max(leftRes[1], rightRes[1])};
        }
    }
}
