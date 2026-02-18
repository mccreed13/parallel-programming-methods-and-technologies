package org.example.CPUBound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class PrimeFinder {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int start = 1;
        int end = 10_000_000;
//        int threads = Runtime.getRuntime().availableProcessors();
        int threads = 12;
        System.out.println("Кількість потоків " + threads);

        long startTime = System.currentTimeMillis();
        List<Integer> primes = findPrimesInRangeConcurrent(start, end, threads);
        Collections.sort(primes);
        long endTime = System.currentTimeMillis();

        System.out.println("Знайдено " + primes.size() + " простих чисел в проміжку [" + start + ", " + end + "]");
        System.out.println("Час виконання: " + (endTime - startTime) + " мс");
    }

    public static boolean isPrime(int n)
    {
        if (n <= 1)
            return false;
        if (n == 2 || n == 3)
            return true;
        if (n % 2 == 0 || n % 3 == 0)
            return false;
        for (int i = 5; i * i <= n; i = i + 6)
            if (n % i == 0 || n % (i + 2) == 0)
                return false;
        return true;
    }


    // A Callable task to find primes in a specific sub-range
    static class PrimeFinderTask implements Callable<List<Integer>> {
        private final int start;
        private final int end;

        public PrimeFinderTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public List<Integer> call() {
            List<Integer> primes = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                if (isPrime(i)) {
                    primes.add(i);
                }
            }
            return primes;
        }
    }

    public static List<Integer> findPrimesInRangeConcurrent(int start, int end, int numThreads) throws InterruptedException, ExecutionException {
        List<Integer> allPrimes = new ArrayList<>();
        try(ExecutorService executor = Executors.newFixedThreadPool(numThreads)){
            List<Future<List<Integer>>> futures = new ArrayList<>();
            int rangeSize = (end - start + 1);
            int segmentSize = rangeSize / numThreads;

            for (int i = 0; i < numThreads; i++) {
                int segmentStart = start + i * segmentSize;
                int segmentEnd = (i == numThreads - 1) ? end : (segmentStart + segmentSize - 1);
                PrimeFinderTask task = new PrimeFinderTask(segmentStart, segmentEnd);
                futures.add(executor.submit(task));
            }

            for (Future<List<Integer>> future : futures) {
                allPrimes.addAll(future.get());
            }

            executor.shutdown();
        }
        return allPrimes;
    }
}
