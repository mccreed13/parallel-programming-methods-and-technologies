package org.example.CPUBound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class PrimeFinder {
    private static final int startRange = 1;
    private static final int endRange = 100_000_000;
    private static final List<Integer> THREADS = List.of(1,6,12);

    public static void main(String[] args) throws InterruptedException{
        System.out.println("Знаходження простих чисел в діапазоні [" + startRange + ", " + endRange +"]");
        for(int threads: THREADS){
            long startTime = System.currentTimeMillis();
            System.out.println("Threads: " + threads);
            find(threads);
            System.out.println("Час виконання: " + (System.currentTimeMillis() - startTime) + " мс");
        }
    }

    public static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;

        // Перевірка дільників до кореня з n
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    private static void find(int numThreads) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Integer> primes = Collections.synchronizedList(new ArrayList<>());

        int chunkSize = (endRange - startRange) / numThreads;

        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int start = startRange + (i * chunkSize);
            final int end = (i == numThreads - 1) ? endRange : (start + chunkSize - 1);

            tasks.add(() -> {
                for (int n = start; n <= end; n++) {
                    if (isPrime(n)) {
                        primes.add(n);
                    }
                }
                return null;
            });
        }
        executor.invokeAll(tasks);
        executor.shutdown();

        System.out.println("Знайдено простих чисел: " + primes.size());
    }
}
