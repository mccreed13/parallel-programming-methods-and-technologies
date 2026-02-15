package org.example.CPU_bound;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Factorization {
    public static List<BigInteger> factorize(BigInteger n) {
        List<BigInteger> factors = new ArrayList<>();
        BigInteger d = BigInteger.valueOf(2);

        while (d.multiply(d).compareTo(n) <= 0) {
            while (n.remainder(d).equals(BigInteger.ZERO)) {
                factors.add(d);
                n = n.divide(d);
            }
            d = d.add(BigInteger.ONE);
        }

        if (n.compareTo(BigInteger.ONE) > 0) {
            factors.add(n);
        }

        return factors;
    }
}
