package org.example.transactions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ProducerConsumerProcessor {
    private static final String POISON_PILL = "STOP"; // Сигнал зупинки
    private static final BlockingQueue<String> queue = new ArrayBlockingQueue<>(1000);

    public static double run(String file) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Producer: Читає файл
        new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) queue.put(line);
                queue.put(POISON_PILL); // Сигналимо про кінець
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        // Consumers: Обробляють дані
        Callable<Double> consumer = () -> {
            double localTotal = 0;
            while (true) {
                String line = queue.take();
                if (line.equals(POISON_PILL)) {
                    queue.put(POISON_PILL); // Повертаємо пігулку іншим
                    break;
                }
                localTotal += processLine(line);
            }
            return localTotal;
        };

        List<Future<Double>> results = new ArrayList<>();
        for (int i = 0; i < 3; i++) results.add(executor.submit(consumer));

        double finalTotal = 0;
        for (Future<Double> f : results) finalTotal += f.get();
        executor.shutdown();
        return finalTotal;
    }

    private static double processLine(String line) {
        String[] parts = line.split(",");
        int userId = Integer.parseInt(parts[0]);
        double amount = Double.parseDouble(parts[1].replace(",", "."));
        String currency = parts[2];

        // 1. Конвертація (спрощено)
        if (currency.equals("USD")) amount *= 44;
        if (currency.equals("EUR")) amount *= 50;

        // 2. Повернення коштів (Cashback)
        if (userId > 1000) amount *= 0.8; // -20%

        return amount;
    }
}
