package org.gerry.solution;

import org.gerry.solution.BankAccountInterest.InterestRule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class DefineInterestRules {
    public static void handleInterestRuleInput(Scanner scanner, List<InterestRule> interestRules) {
        System.out.println("\nPlease enter interest rules details in <Date> <RuleId> <Rate in %> format");
        System.out.println("(or enter blank to go back to main menu):");
        System.out.print("> ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        String[] parts = input.split("\\s+");
        if (parts.length != 3) {
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

            // Parse rule ID
            String ruleId = parts[1];

            // Parse interest rate
            BigDecimal rate;
            try {
                rate = new BigDecimal(parts[2]);
                if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(new BigDecimal("100")) >= 0) {
                    System.out.println("Interest rate must be greater than 0 and less than 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid rate format.");
                return;
            }

            // Remove any existing rule for the same date
            interestRules.removeIf(rule -> rule.effectiveDate().equals(date));

            // Add the new rule
            interestRules.add(new InterestRule(date, ruleId, rate));

            // Sort rules by date
            Collections.sort(interestRules);

            // Display all interest rules
            System.out.println("\nInterest rules:");
            System.out.println("| Date     | RuleId | Rate (%) |");
            for (InterestRule rule : interestRules) {
                System.out.printf("| %s | %-6s | %8.2f |\n",
                        rule.effectiveDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                        rule.ruleId(),
                        rule.rate());
            }
        } catch (Exception e) {
            System.out.println("Error processing interest rule: " + e.getMessage());
        }
    }
}