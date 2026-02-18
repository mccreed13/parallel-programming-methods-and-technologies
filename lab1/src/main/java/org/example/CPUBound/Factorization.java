package org.example.CPUBound;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Factorization {
    private static final ExecutorService executor = Executors.newFixedThreadPool(12);
    private static final List<BigInteger> primeFactors = Collections.synchronizedList(new ArrayList<>());
    private static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) throws InterruptedException {
        BigInteger number = new BigInteger("10293847561122334455987654321055667788991357924680246801357991827364501029384756"); // Велике число

        System.out.println("Починаємо повну факторизацію: " + number);
        long startTime = System.currentTimeMillis();

        factorize(number);

        // Чекаємо завершення всіх задач
        executor.shutdown();
        if (executor.awaitTermination(1, TimeUnit.MINUTES)) {
            Collections.sort(primeFactors);
            System.out.println("\nПрості множники: " + primeFactors);
            System.out.println("Час виконання: " + (System.currentTimeMillis() - startTime) + " мс");
        }
    }

    public static void factorize(BigInteger n) {
        if (n.equals(BigInteger.ONE)) return;
        if (n.isProbablePrime(20)) {
            primeFactors.add(n);
            return;
        }
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            primeFactors.add(BigInteger.TWO);
            factorize(n.divide(BigInteger.TWO));
            return;
        }
        try {
            BigInteger factor = findFactorParallel(n);
            factorize(factor);
            factorize(n.divide(factor));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BigInteger findFactorParallel(BigInteger n) throws InterruptedException, ExecutionException {
        int threads = Runtime.getRuntime().availableProcessors();
        CompletionService<BigInteger> service = new ExecutorCompletionService<>(executor);

        // Використовуємо AtomicBoolean для сигналізації іншим потокам про успіх
        AtomicBoolean found = new AtomicBoolean(false);

        for (int i = 0; i < threads; i++) {
            service.submit(() -> {
                BigInteger x = new BigInteger(n.bitLength(), random).mod(n);
                BigInteger y = x;
                BigInteger c = new BigInteger(n.bitLength(), random).mod(n);
                BigInteger d = BigInteger.ONE;

                while (d.equals(BigInteger.ONE) && !found.get()) {
                    x = x.multiply(x).add(c).mod(n);
                    y = y.multiply(y).add(c).mod(n);
                    y = y.multiply(y).add(c).mod(n);
                    d = x.subtract(y).abs().gcd(n);

                    if (d.equals(n)) return null; // Невдача для цієї константи c
                }

                if (!d.equals(BigInteger.ONE)) {
                    found.set(true);
                    return d;
                }
                return null;
            });
        }

        while (true) {
            Future<BigInteger> result = service.take();
            BigInteger res = result.get();
            if (res != null) return res;

            // Якщо всі потоки повернули null, перезапускаємо (рідкісний випадок)
            if (!found.get()) return findFactorParallel(n);
        }
    }
}
