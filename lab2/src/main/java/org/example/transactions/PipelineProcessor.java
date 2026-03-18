package org.example.transactions;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

public class PipelineProcessor {
    public static double run(String file) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(file))) {
            return lines.parallel()
                    .map(line -> line.split(","))
                    // Етап 1: Мапінг у об'єкт та Конвертація
                    .map(p -> {
                        double val = Double.parseDouble(p[1].replace(",", "."));
                        if (p[2].equals("USD")) val *= 38;
                        else if (p[2].equals("EUR")) val *= 40;
                        return new Object[]{p[0], val}; // [userId, amountUAH]
                    })
                    // Етап 2: Розрахунок кешбеку
                    .mapToDouble(obj -> {
                        int id = Integer.parseInt((String)obj[0]);
                        double amt = (Double)obj[1];
                        return (id > 1000) ? amt * 0.8 : amt;
                    })
                    // Етап 3: Агрегація
                    .sum();
        }
    }
}
