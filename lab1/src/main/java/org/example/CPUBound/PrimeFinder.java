package org.example.CPUBound;

import java.util.Arrays;

public class PrimeFinder {
    static boolean[] sieveOfEratosthenes(int n) {
        boolean[] prime = new boolean[n + 1];
        Arrays.fill(prime, true);

        // Mark 0 and 1 as non-prime
        prime[0] = false;
        prime[1] = false;

        // Loop through numbers from 2 to sqrt(n)
        // to mark their multiples as non-prime
        for (int p = 2; p * p <= n; p++) {

            // If prime[p] is still true, it means 'p' is
            // prime
            if (prime[p]) {

                // Mark all multiples of p greater
                // than or equal to p^2 as non-prime
                // Numbers less than p^2 would
                // have already been marked as non-prime
                for (int i = p * p; i <= n; i += p)
                    prime[i] = false;
            }
        }

        return prime;
    }

    static int[] primeRange(int m, int n) {

        // Get the boolean array representing prime numbers
        // up to n
        boolean[] isPrime = sieveOfEratosthenes(n);

        // Count the number of primes in the range [m, n]
        int count = 0;
        for (int i = m; i <= n; i++) {
            if (isPrime[i])
                count++;
        }

        // Create an array to store the prime numbers
        int[] ans = new int[count];
        int index = 0;

        // Loop through the range [m, n] and collect all
        // prime numbers
        for (int i = m; i <= n; i++) {
            if (isPrime[i]) {
                ans[index++] = i;
            }
        }

        return ans;
    }

}
