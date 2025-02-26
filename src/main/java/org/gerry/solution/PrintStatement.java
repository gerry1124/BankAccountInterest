package org.gerry.solution;

import org.gerry.solution.BankAccountInterest.Transaction;
import org.gerry.solution.BankAccountInterest.InterestRule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PrintStatement {
    public static void handleStatementPrint(Scanner scanner, Map<String, List<Transaction>> accounts,
                                            List<InterestRule> interestRules) {
        String input = getUserInput(scanner);
        if (input == null) return;

        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Invalid format. Please try again.");
            return;
        }

        String accountId = parts[0];
        if (!accounts.containsKey(accountId)) {
            System.out.println("Account not found.");
            return;
        }

        int year, month;
        if (!isValidYearMonth(parts[1])) {
            System.out.println("Invalid year/month format. Use YYYYMM format.");
            return;
        }
        year = Integer.parseInt(parts[1].substring(0, 4));
        month = Integer.parseInt(parts[1].substring(4, 6));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        List<Transaction> accountTransactions = accounts.get(accountId);

        BigDecimal openingBalance = calculateOpeningBalance(accountTransactions, startDate);
        List<Transaction> monthTransactions = filterTransactionsByDate(accountTransactions, startDate, endDate);
        monthTransactions.sort(Comparator.comparing(Transaction::date));

        BigDecimal interest = calculateInterest(accountTransactions, interestRules, year, month);
        Transaction interestTransaction = new Transaction(endDate, accountId, 'I', interest, "           ");

        printAccountStatement(accountId, monthTransactions, openingBalance, interestTransaction);
    }

    private static String getUserInput(Scanner scanner) {
        System.out.println("\nPlease enter account and month to generate the statement <Account> <Year><Month>");
        System.out.println("(or enter blank to go back to main menu):");
        System.out.print("> ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    private static boolean isValidYearMonth(String input) {
        if (input.length() != 6) return false;
        try {
            int month = Integer.parseInt(input.substring(4, 6));
            return month >= 1 && month <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static List<Transaction> filterTransactionsByDate(List<Transaction> transactions,
                                                              LocalDate startDate, LocalDate endDate) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!t.date().isBefore(startDate) && !t.date().isAfter(endDate)) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    private static void printAccountStatement(String accountId, List<Transaction> transactions,
                                              BigDecimal openingBalance, Transaction interestTransaction) {
        System.out.println("\nAccount: " + accountId);
        System.out.println("| Date     | Txn Id      | Type | Amount  | Balance |");

        BigDecimal runningBalance = openingBalance;
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Transaction t : transactions) {
            runningBalance = updateBalance(runningBalance, t);
            System.out.printf("| %s | %-10s | %-4s | %7s | %7s |\n",
                    t.date().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    t.transactionId(),
                    t.type(),
                    df.format(t.amount()),
                    df.format(runningBalance));
        }

        runningBalance = runningBalance.add(interestTransaction.amount());
        System.out.printf("| %s | %-10s | %-4s | %7s | %7s |\n",
                interestTransaction.date().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                interestTransaction.transactionId(),
                interestTransaction.type(),
                df.format(interestTransaction.amount()),
                df.format(runningBalance));
    }

    private static BigDecimal updateBalance(BigDecimal balance, Transaction t) {
        return t.type() == 'D' ? balance.add(t.amount()) : balance.subtract(t.amount());
    }

    private static BigDecimal calculateOpeningBalance(List<Transaction> transactions, LocalDate startDate) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.date().isBefore(startDate)) {
                balance = updateBalance(balance, t);
            }
        }
        return balance;
    }

    private static BigDecimal calculateInterest(List<Transaction> transactions,
                                                List<InterestRule> interestRules,
                                                int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        Set<LocalDate> significantDates = new TreeSet<>();
        significantDates.add(startDate);
        significantDates.add(endDate);

        for (Transaction t : transactions) {
            if (!t.date().isBefore(startDate) && !t.date().isAfter(endDate)) {
                significantDates.add(t.date());
            }
        }

        for (InterestRule rule : interestRules) {
            if (!rule.effectiveDate().isAfter(endDate) && !rule.effectiveDate().isBefore(startDate)) {
                significantDates.add(rule.effectiveDate());
            }
        }

        List<LocalDate> datesList = new ArrayList<>(significantDates);
        BigDecimal totalInterest = BigDecimal.ZERO;

        //System.out.println("Interest Calculation Breakdown:");

        for (int i = 0; i < datesList.size() - 1; i++) {
            LocalDate periodStart = datesList.get(i);
            LocalDate periodEnd = datesList.get(i + 1).minusDays(1);

            if (i == datesList.size() - 2) {
                periodEnd = endDate;
            }

            BigDecimal eodBalance = calculateBalanceAtDate(transactions, periodStart);
            InterestRule applicableRule = findApplicableRule(interestRules, periodStart);

            if (applicableRule != null) {
                int days = (int) ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
                BigDecimal periodInterest = eodBalance
                        .multiply(applicableRule.rate())
                        .multiply(new BigDecimal(days))
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

                //System.out.printf("From %s to %s -> Balance: %.2f, Rate: %.2f%%, Days: %d, Interest: %.2f\n",
                //        periodStart, periodEnd, eodBalance, applicableRule.rate(), days, periodInterest);

                totalInterest = totalInterest.add(periodInterest);
            }
        }

        totalInterest = totalInterest.divide(new BigDecimal("365"), 4, RoundingMode.HALF_UP);
        //System.out.printf("Total Interest Before Rounding: %.4f\n", totalInterest);
        totalInterest = totalInterest.setScale(2, RoundingMode.HALF_UP);
        //System.out.printf("Final Interest After Rounding: %.2f\n", totalInterest);

        return totalInterest;
    }

    private static BigDecimal calculateBalanceAtDate(List<Transaction> transactions, LocalDate date) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (!t.date().isAfter(date)) {
                balance = updateBalance(balance, t);
            }
        }
        return balance;
    }

    private static InterestRule findApplicableRule(List<InterestRule> rules, LocalDate date) {
        return rules.stream()
                .filter(rule -> !rule.effectiveDate().isAfter(date))
                .max(Comparator.comparing(InterestRule::effectiveDate))
                .orElse(null);
    }
}