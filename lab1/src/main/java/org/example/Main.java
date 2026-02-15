package org.example;

import org.example.CPU_bound.Factorization;
import org.example.CPU_bound.PiCalculator;

import java.math.BigInteger;
import java.sql.Time;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println(PiCalculator.calculate(1_000_000_000));
        long endTime = System.currentTimeMillis();
        System.out.println("Sequence time = " + (endTime-startTime));

        startTime = System.currentTimeMillis();
        System.out.println(PiCalculator.calculateInParallel(1_000_000_000, 2));
        endTime = System.currentTimeMillis();
        System.out.println("Parallel time = " + (endTime-startTime));
    }
}