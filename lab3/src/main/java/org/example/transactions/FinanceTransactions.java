package org.example.transactions;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FinanceTransactions {
    private static final int TRANSACTION_ACCOUNTS = 100;
    private static final int BALANCE_MIN = 100;
    private static final int BALANCE_MAX = 10000;
    private static final int THREADS = 1000;
    private static final int OPERATIONS = 1_000_000;

    public static void main(String[] args) throws Exception {
        System.out.println("Safe Finance Transactions");
        mainFlow(0);
        System.out.println("---------------------------");
        System.out.println("Race Condition Finance Transactions");
        mainFlow(1);
        System.out.println("---------------------------");
        System.out.println("Deadlock Finance Transactions");
        mainFlow(2);
    }

    private static void mainFlow(int var) throws InterruptedException {
        List<BankAccount> bankAccounts = generateAccounts(TRANSACTION_ACCOUNTS, BALANCE_MIN, BALANCE_MAX);
        BigInteger totalBefore = getTotalBalance(bankAccounts);
        System.out.println("Загальна сума до: " + totalBefore);
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        Random random = new Random();
        for (int i = 0; i < OPERATIONS; i++) {
            executor.execute(() -> {
                BankAccount from = bankAccounts.get(random.nextInt(TRANSACTION_ACCOUNTS));
                BankAccount to = bankAccounts.get(random.nextInt(TRANSACTION_ACCOUNTS));
                if (from == to) return;

                int amount = random.nextInt(500);
                switch (var) {
                    case 0:
                        safeTransfer(from, to, amount);
                        break;
                    case 1:
                        raceConditionTransfer(from, to, amount);
                        break;
                    case 2:
                        deadLockTransfer(from, to, amount);
                        break;
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS);

        BigInteger totalAfter = getTotalBalance(bankAccounts);
        System.out.println("Загальна сума після: " + totalAfter);
        if (!Objects.equals(totalBefore, totalAfter)) {
            System.out.println("Різниця: " + (totalBefore.subtract(totalAfter)));
        } else {
            System.out.println("Різниці немає");
        }
        deadLockDetector(executor);
    }

    public static void deadLockTransfer(BankAccount from, BankAccount to, int amount) {
        synchronized (from) {
            synchronized (to) { // Якщо Thread 1: A->B, а Thread 2: B->A — стається Deadlock
                if (from.getBalance() >= amount) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        }
    }

    public static void raceConditionTransfer(BankAccount from, BankAccount to, int amount) {
        if (from.getBalance() >= amount) {
            from.withdraw(amount);
            to.deposit(amount);
        }
    }

    public static void safeTransfer(BankAccount from, BankAccount to, int amount) {
        // Вирішення Deadlock: завжди блокуємо об'єкти в однаковому порядку (за ID)
        BankAccount firstLock = from.getId() < to.getId() ? from : to;
        BankAccount secondLock = from.getId() < to.getId() ? to : from;

        synchronized (firstLock.getLock()) {
            synchronized (secondLock.getLock()) {
                if (from.getBalance() >= amount) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        }
    }

    public static void deadLockDetector(ExecutorService executor) {
        if (!executor.isTerminated()) {
            System.err.println("\n--- ВИЯВЛЕНО ЗАТРИМКУ: Аналіз Deadlock ---");

            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            long[] deadlockedThreads = threadBean.findDeadlockedThreads();

            if (deadlockedThreads != null) {
                System.err.println("Знайдено Deadlock! Кількість застряглих потоків: " + deadlockedThreads.length);

                ThreadInfo[] threadInfos = threadBean.getThreadInfo(deadlockedThreads);
                for (ThreadInfo info : threadInfos) {
                    System.err.println("Потік: " + info.getThreadName() + " (ID: " + info.getThreadId() + ")");
                    System.err.println("  Статус: " + info.getThreadState());
                    System.err.println("  Чекає на лок об'єкта: " + info.getLockName());
                    System.err.println("  Власник цього локу: " + info.getLockOwnerName());
                    System.err.println("--------------------------------------");
                }
            } else {
                System.err.println("Deadlock не знайдено, можливо потоки просто дуже повільні.");
            }

            // Примусово завершуємо програму
            executor.shutdownNow();
        }
    }

    public static List<BankAccount> generateAccounts(int accCount, int balanceMin, int balanceMax) {
        List<BankAccount> bankAccounts = new ArrayList<>();
        Random r = new Random();
        for (int i = 1; i <= accCount; i++) {
            bankAccounts.add(new BankAccount(i, balanceMin + (int) (r.nextDouble(1.0) * (balanceMax - balanceMin))));
        }
        return bankAccounts;
    }

    private static BigInteger getTotalBalance(List<BankAccount> bankAccounts) {
        BigInteger total = BigInteger.ZERO;
        for (BankAccount bankAccount : bankAccounts) {
            total = total.add(BigInteger.valueOf(bankAccount.getBalance()));
        }
        return total;
    }
}
