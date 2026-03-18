package org.example.patterns;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class MultiplicationMatrices {
    private final static int M = 1000;
    private final static int N = 1200;
    private final static int K = 1000;

    public static void main(String[] args) {
        double[][] matrixA = new double[M][N];
        double[][] matrixB = new double[N][K];
        long start = System.currentTimeMillis();
        multiplySequential(matrixA, matrixB);
        System.out.println("Sequential: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("---------------------------------------------------------");
        start = System.currentTimeMillis();
        multiplyParallelStream(matrixA, matrixB);
        System.out.println("MapReduce: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("---------------------------------------------------------");
        try {
            start = System.currentTimeMillis();
            multiplyWorkerPool(matrixA, matrixB);
            System.out.println("Worker Pool: " + (System.currentTimeMillis() - start) + "ms");
            System.out.println("---------------------------------------------------------");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double[][] multiplySequential(double[][] A, double[][] B) {
        double[][] C = new double[M][K];
        for (int i = 0; i < M; i++) {
            for (int k = 0; k < N; k++) { // k у другому циклі для кращого використання кешу (Cache-friendly)
                for (int j = 0; j < K; j++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    public static double[][] multiplyParallelStream(double[][] A, double[][] B) {
        double[][] C = new double[M][K];
        IntStream.range(0, M).parallel().forEach(i -> {
            for (int k = 0; k < N; k++) {
                for (int j = 0; j < K; j++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        });
        return C;
    }

    public static double[][] multiplyWorkerPool(double[][] A, double[][] B) throws Exception {
        double[][] C = new double[M][K];
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        int rowPerTask = M / cores;
        for (int i = 0; i < cores; i++) {
            int startRow = i * rowPerTask;
            int endRow = (i == cores - 1) ? M : (i + 1) * rowPerTask;

            executor.execute(() -> {
                for (int r = startRow; r < endRow; r++) {
                    for (int k = 0; k < N; k++) {
                        for (int j = 0; j < K; j++) {
                            C[r][j] += A[r][k] * B[k][j];
                        }
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        return C;
    }
}
