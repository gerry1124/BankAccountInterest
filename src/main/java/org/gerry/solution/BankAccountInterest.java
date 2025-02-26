package org.gerry.solution;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class BankAccountInterest {
    private static Scanner scanner = new Scanner(System.in);
    private static final Map<String, List<Transaction>> accounts = new HashMap<>();
    private static final List<InterestRule> interestRules = new ArrayList<>();
    private static final Map<String, Integer> transactionCountByDate = new HashMap<>();
    public static void setScanner(Scanner customScanner) {
        scanner = customScanner;
    }

    public static void main(String[] args) {
        // Default Interest : 1.95
        interestRules.add(new InterestRule(LocalDate.of(2023, 1, 1),
                "RULE01", new BigDecimal("1.95")));

        boolean isAppRunning = true;
        boolean isFirstPrompt = true;

        while (isAppRunning) {
            if (isFirstPrompt) {
                displayMainMenu();
                isFirstPrompt = false;
            } else {
                displaySubMenu();
            }

            if (!scanner.hasNextLine()) {
                break;
            }

            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.isEmpty() || choice.charAt(0) == 'Q') {
                isAppRunning = false;
                System.out.println("\nThank you for banking with AwesomeGIC Bank.");
                System.out.println("Have a nice day!");
            } else if (choice.charAt(0) == 'T') {
                InputTransaction.handleTransactionInput(scanner, accounts, transactionCountByDate);
            } else if (choice.charAt(0) == 'I') {
                DefineInterestRules.handleInterestRuleInput(scanner, interestRules);
            } else if (choice.charAt(0) == 'P') {
                PrintStatement.handleStatementPrint(scanner, accounts, interestRules);
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println("\nWelcome to AwesomeGIC Bank! What would you like to do?");
        displayMenuOptions();
    }

    public static void displaySubMenu() {
        System.out.println("\nIs there anything else you'd like to do?");
        displayMenuOptions();
    }

    private static void displayMenuOptions() {
        System.out.println("[T] Input Transactions");
        System.out.println("[I] Define Interest Rules");
        System.out.println("[P] Print Statement");
        System.out.println("[Q] Quit");
        System.out.print("> ");
    }

    // 2 record classes for Transaction and InterestRule to model data easily
    public record Transaction(LocalDate date, String accountId, char type, BigDecimal amount, String transactionId) {
    }

    public record InterestRule(LocalDate effectiveDate, String ruleId, BigDecimal rate)
            implements Comparable<InterestRule> {

        @Override
            public int compareTo(InterestRule other) {
                return this.effectiveDate.compareTo(other.effectiveDate);
            }
        }
}