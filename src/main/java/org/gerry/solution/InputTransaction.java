package org.gerry.solution;

import org.gerry.solution.BankAccountInterest.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InputTransaction {
    public static void handleTransactionInput(Scanner scanner, Map<String, List<Transaction>> accounts,
                                              Map<String, Integer> transactionCountByDate) {
        System.out.println("\nPlease enter transaction details in <Date> <Account> <Type> <Amount> format");
        System.out.println("(or enter blank to go back to main menu):");
        System.out.print("> ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        String[] parts = input.split("\\s+");
        if (parts.length != 4) {
            System.out.println("Invalid format. Please try again.");
            return;
        }

        try {
            // Parse date
            LocalDate date;
            try {
                date = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use YYYYMMdd format.");
                return;
            }

            // Parse account
            String accountId = parts[1];

            // Parse transaction type
            char type = parts[2].toUpperCase().charAt(0);
            if (type != 'D' && type != 'W') {
                System.out.println("Invalid transaction type. Use D for deposit or W for withdrawal.");
                return;
            }

            // Parse amount
            BigDecimal amount;
            try {
                amount = new BigDecimal(parts[3]).setScale(2, RoundingMode.HALF_UP);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount format.");
                return;
            }

            // Check if it's a new account or if withdrawal is valid
            if (!accounts.containsKey(accountId)) {
                if (type == 'W') {
                    System.out.println("First transaction for an account cannot be a withdrawal.");
                    return;
                }
                accounts.put(accountId, new ArrayList<>());
            } else if (type == 'W') {
                // Check if withdrawal would make balance negative
                BigDecimal currentBalance = calculateBalance(accounts.get(accountId));
                if (currentBalance.compareTo(amount) < 0) {
                    System.out.println("Insufficient funds. Current balance: " + currentBalance);
                    return;
                }
            }

            // Generate transaction ID
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int count = transactionCountByDate.getOrDefault(dateStr, 0) + 1;
            transactionCountByDate.put(dateStr, count);
            String transactionId = dateStr + "-" + String.format("%02d", count);

            // Create and add the transaction
            Transaction transaction = new Transaction(date, accountId, type, amount, transactionId);
            accounts.get(accountId).add(transaction);

            // Display the account statement
            System.out.println("\nAccount: " + accountId);
            System.out.println("| Date     | Txn Id      | Type | Amount |");
            for (Transaction t : accounts.get(accountId)) {
                System.out.printf("| %s | %-10s | %-4s | %6.2f |\n",
                        t.date().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                        t.transactionId(),
                        String.valueOf(t.type()),
                        t.amount());
            }
        } catch (Exception e) {
            System.out.println("Error processing transaction: " + e.getMessage());
        }
    }

    private static BigDecimal calculateBalance(List<Transaction> transactions) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.type() == 'D') {
                balance = balance.add(t.amount());
            } else if (t.type() == 'W') {
                balance = balance.subtract(t.amount());
            }
        }
        return balance;
    }
}