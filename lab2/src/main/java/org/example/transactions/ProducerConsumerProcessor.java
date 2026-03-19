package org.example.transactions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.DoubleAdder;

public class ProducerConsumerProcessor {
    private static final int QUEUE_CAPACITY = 5000;
    private static final int CONSUMER_THREADS = Runtime.getRuntime().availableProcessors();
    private static final String POISON_PILL = "EOF";

    private static final DoubleAdder finalTotalAmount = new DoubleAdder();

    public static double run(String filePath) throws Exception {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        ExecutorService executor = Executors.newFixedThreadPool(CONSUMER_THREADS);

        Thread producer = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    queue.put(line);
                }
                for (int i = 0; i < CONSUMER_THREADS; i++) {
                    queue.put(POISON_PILL);
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        for (int i = 0; i < CONSUMER_THREADS; i++) {
            executor.execute(() -> {
                try {
                    while (true) {
                        String line = queue.take();
                        if (line.equals(POISON_PILL)) break;

                        double processedAmount = processTransaction(line);
                        finalTotalAmount.add(processedAmount);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        producer.start();
        producer.join();


        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        return finalTotalAmount.sum();
    }

    private static double processTransaction(String line) {
        try {
            String[] p = line.split(",");
            int userId = Integer.parseInt(p[0]);
            double amount = Double.parseDouble(p[1].replace(",", "."));
            String currency = p[2];

            // Етап Конвертації (Валюта -> UAH)
            switch (currency) {
                case "USD" -> amount *= 43;
                case "EUR" -> amount *= 50;
                default -> {
                } // UAH залишається як є
            }

            // Етап Кешбеку: якщо ID > 1000, повертаємо 20% (тобто залишається 80% суми)
            if (userId > 1000) {
                amount *= 0.8;
            }

            return amount;
        } catch (Exception e) {
            return 0; // Ігноруємо биті рядки
        }
    }

}
