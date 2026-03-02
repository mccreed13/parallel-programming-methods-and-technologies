package org.example.IOBound;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class RecursiveWordCounter {
    private static final List<Integer> THREADS = List.of(1,6,12);

    public static void main(String[] args) throws Exception {
        Path testDir = Paths.get("data_samples");
        generateRandomStructure(testDir, 1000);
        for (int threads : THREADS) {
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            AtomicLong totalWordCount = new AtomicLong(0);
            long startTime = System.currentTimeMillis();
            walkAndSubmit(testDir.toFile(), executor, totalWordCount);
            executor.shutdown();
            if (executor.awaitTermination(10, TimeUnit.MINUTES)) {
                System.out.println("Кількість потоків: " + threads);
                System.out.println("Загальна кількість слів: " + totalWordCount.get());
                System.out.println("Час виконання: " + (System.currentTimeMillis() - startTime) + " мс");
            }
        }
        deleteDirectory(testDir.toFile());
    }

    private static void walkAndSubmit(File file, ExecutorService executor, AtomicLong counter) {
        File[] list = file.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) {
                walkAndSubmit(f, executor, counter);
            } else if (f.getName().endsWith(".txt")) {
                executor.submit(() -> {
                    long wordsInFile = countWords(f);
                    counter.addAndGet(wordsInFile);
                });
            }
        }
    }

    private static long countWords(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines()
                    .flatMap(line -> Arrays.stream(line.split("\\s+")))
                    .filter(word -> !word.isEmpty())
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }

    private static void generateRandomStructure(Path root, int totalFiles) throws IOException {
        if (Files.exists(root)) deleteDirectory(root.toFile());
        Files.createDirectory(root);

        Random random = new Random();
        for (int i = 0; i < totalFiles; i++) {
            Path currentDir = root;
            if (random.nextBoolean()) {
                currentDir = root.resolve("subdir_" + random.nextInt(10));
                Files.createDirectories(currentDir);
            }

            Path filePath = currentDir.resolve("file_" + i + ".txt");
            String content = "Java concurrency is powerful and fast. ".repeat(random.nextInt(50) + 1);
            Files.writeString(filePath, content);
        }
        System.out.println("Згенеровано 1000 файлів у " + root.toAbsolutePath());
    }

    private static void deleteDirectory(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        folder.delete();
    }
}
