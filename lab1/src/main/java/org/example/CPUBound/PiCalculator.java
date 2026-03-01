package org.example.CPUBound;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PiCalculator {
    private static final List<Integer> THREADS = List.of(1,6,12);
    private static final long totalIterations = 1_000_000_000L;


    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("Обчислення " + totalIterations + " точок");
        for (int threads : THREADS) {
            long startTime = System.currentTimeMillis();
            double pi = calculate(threads);
            System.out.println("Результат ПІ: " + pi);
            System.out.println("Час виконання: " + (System.currentTimeMillis() - startTime) + " мс");
        }
    }

    private static double calculate(int numThreads) throws ExecutionException, InterruptedException {
        long iterationsPerThread = PiCalculator.totalIterations / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Long>> results = new ArrayList<>();

        System.out.println("Запуск обчислень на " + numThreads + " потоках...");

        // Розподіляємо роботу між потоками
        for (int i = 0; i < numThreads; i++) {
            results.add(executor.submit(() -> {
                long count = 0;
                ThreadLocalRandom random = ThreadLocalRandom.current();

                for (long j = 0; j < iterationsPerThread; j++) {
                    double x = random.nextDouble();
                    double y = random.nextDouble();
                    // Перевіряємо, чи потрапила точка в чверть кола (x^2 + y^2 <= 1)
                    if (x * x + y * y <= 1) {
                        count++;
                    }
                }
                return count;
            }));
        }

        long totalInCircle = 0;
        for (Future<Long> res : results) {
            totalInCircle += res.get();
        }
        executor.shutdown();

        return  4.0 * totalInCircle / (iterationsPerThread * numThreads);
    }

}
