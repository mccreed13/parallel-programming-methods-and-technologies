package org.example.MemoryBound;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelMatrixTranspose {
    private static final int SIZE = 10000;
    private static final double[] MATRIX = new double[SIZE * SIZE];
    private static final List<Integer> THREADS = List.of(1,6,12);


    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < MATRIX.length; i++) {
            MATRIX[i] = i;
        }
        for (int threads : THREADS) {
            System.out.println("Кількість потоків: " + threads);
            long startTime = System.currentTimeMillis();
            transpose(threads);
            System.out.println("Час виконання: " + (System.currentTimeMillis() - startTime) + " мс");
        }
    }

    private static void transpose(int numThreads) throws InterruptedException {
        double[] result = new double[SIZE * SIZE];
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int rowsPerThread = SIZE / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = (t == numThreads - 1) ? SIZE : (startRow + rowsPerThread);

            executor.submit(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        result[j * SIZE + i] = MATRIX[i * SIZE + j];
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

         assert result[SIZE] == MATRIX[1];
    }
}
