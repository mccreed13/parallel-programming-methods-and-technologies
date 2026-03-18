package org.example.transactions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class FinanceTransactions {
    private static final int TRANSACTION_NAMES = 1_000_000;

    public static void main(String[] args) throws Exception {
        String filename = "transactions.csv";
        generate(filename, TRANSACTION_NAMES);

        System.out.println("Processing with Producer-Consumer...");
        long start = System.currentTimeMillis();
        double res1 = ProducerConsumerProcessor.run(filename);
        System.out.println("Total: " + res1 + " | Time: " + (System.currentTimeMillis()-start) + "ms");

        System.out.println("Processing with Pipeline...");
        start = System.currentTimeMillis();
        double res2 = PipelineProcessor.run(filename);
        System.out.println("Total: " + res2 + " | Time: " + (System.currentTimeMillis()-start) + "ms");
    }


    public static void generate(String filename, int count) throws IOException {
        String[] currencies = {"USD", "EUR", "UAH"};
        String[] categories = {"Electronics", "Food", "Clothing", "Service"};
        Random r = new Random();

        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            for (int i = 0; i < count; i++) {
                // userId, amount, currency, date, category
                pw.printf("%d,%.2f,%s,2026-03-18,%s\n",
                        r.nextInt(2000),           // ID користувача
                        10.0 + r.nextDouble() * 1000, // Сума
                        currencies[r.nextInt(3)],  // Валюта
                        categories[r.nextInt(4)]   // Категорія
                );
            }
        }
    }
}
