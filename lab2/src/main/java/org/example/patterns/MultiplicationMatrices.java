package org.example.patterns;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.concurrent.ForkJoinTask.invokeAll;

public class MultiplicationMatrices {
    private final static int M = 1000;
    private final static int N = 1000;
    private final static int K = 1000;

    public static void main(String[] args) {
        double[][] matrixA = new double[M][N];
        double[][] matrixB = new double[N][K];
        long start = System.currentTimeMillis();
        multiplySequential(matrixA, matrixB, M);
        System.out.println("Sequential: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("---------------------------------------------------------");
        start = System.currentTimeMillis();
        multiplyParallelStream(matrixA, matrixB, M);
        System.out.println("MapReduce: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("---------------------------------------------------------");
        try {
            start = System.currentTimeMillis();
            multiplyWorkerPool(matrixA, matrixB, M);
            System.out.println("Worker Pool: " + (System.currentTimeMillis() - start) + "ms");
            System.out.println("---------------------------------------------------------");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        start = System.currentTimeMillis();
//        new MatrixForkJoinTask(matrixA,
//                               matrixB,
//                               new double[M][K]
//                ).compute();
//        System.out.println("Worker Pool: " + (System.currentTimeMillis() - start) + "ms");
//        System.out.println("---------------------------------------------------------");
    }

    public static double[][] multiplySequential(double[][] A, double[][] B, int n) {
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) { // k у другому циклі для кращого використання кешу (Cache-friendly)
                for (int j = 0; j < n; j++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    public static double[][] multiplyParallelStream(double[][] A, double[][] B, int n) {
        double[][] C = new double[n][n];
        IntStream.range(0, n).parallel().forEach(i -> {
            for (int k = 0; k < n; k++) {
                for (int j = 0; j < n; j++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        });
        return C;
    }

    public static double[][] multiplyWorkerPool(double[][] A, double[][] B, int n) throws Exception {
        double[][] C = new double[n][n];
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        int rowPerTask = n / cores;
        for (int i = 0; i < cores; i++) {
            int startRow = i * rowPerTask;
            int endRow = (i == cores - 1) ? n : (i + 1) * rowPerTask;

            executor.execute(() -> {
                for (int r = startRow; r < endRow; r++) {
                    for (int k = 0; k < n; k++) {
                        for (int j = 0; j < n; j++) {
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

    public class MatrixForkJoinTask extends RecursiveAction {
        private final double[][] A, B, C;
        private final int rowA, colA, rowB, colB, rowC, colC, size;
        private static final int THRESHOLD = 64; // Розмір блоку, який обробляється циклом

        public MatrixForkJoinTask(double[][] A, int rowA, int colA,
                                  double[][] B, int rowB, int colB,
                                  double[][] C, int rowC, int colC, int size) {
            this.A = A; this.rowA = rowA; this.colA = colA;
            this.B = B; this.rowB = rowB; this.colB = colB;
            this.C = C; this.rowC = rowC; this.colC = colC;
            this.size = size;
        }

        @Override
        protected void compute() {
            if (size <= THRESHOLD) {
                // Базовий випадок: звичайне множення малих блоків
                for (int i = 0; i < size; i++) {
                    for (int k = 0; k < size; k++) {
                        for (int j = 0; j < size; j++) {
                            C[rowC + i][colC + j] += A[rowA + i][colA + k] * B[rowB + k][colB + j];
                        }
                    }
                }
            } else {
                int newSize = size / 2;
                // У спрощеному вигляді ми запускаємо 8 підзадач для кожного квадранта
                // Для реального множення C = A * B нам потрібно обчислити:
                // C11 = A11*B11 + A12*B21, і так далі.

                invokeAll(
                        new MatrixForkJoinTask(A, rowA, colA, B, rowB, colB, C, rowC, colC, newSize),
                        new MatrixForkJoinTask(A, rowA, colA + newSize, B, rowB + newSize, colB, C, rowC, colC, newSize),

                        new MatrixForkJoinTask(A, rowA, colA, B, rowB, colB + newSize, C, rowC, colC + newSize, newSize),
                        new MatrixForkJoinTask(A, rowA, colA + newSize, B, rowB + newSize, colB + newSize, C, rowC, colC + newSize, newSize),

                        new MatrixForkJoinTask(A, rowA + newSize, colA, B, rowB, colB, C, rowC + newSize, colC, newSize),
                        new MatrixForkJoinTask(A, rowA + newSize, colA + newSize, B, rowB + newSize, colB, C, rowC + newSize, colC, newSize),

                        new MatrixForkJoinTask(A, rowA + newSize, colA, B, rowB, colB + newSize, C, rowC + newSize, colC + newSize, newSize),
                        new MatrixForkJoinTask(A, rowA + newSize, colA + newSize, B, rowB + newSize, colB + newSize, C, rowC + newSize, colC + newSize, newSize)
                );
            }
        }
    }
}
