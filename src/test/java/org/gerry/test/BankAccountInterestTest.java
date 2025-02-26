package org.gerry.test;

import org.gerry.solution.BankAccountInterest;
import org.gerry.solution.BankAccountInterest.*;
import org.gerry.solution.DefineInterestRules;
import org.gerry.solution.InputTransaction;
import org.gerry.solution.PrintStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class BankAccountInterestTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @SuppressWarnings("unchecked")
    private Map<String, List<Transaction>> getAccounts() throws Exception {
        Field accountsField = BankAccountInterest.class.getDeclaredField("accounts");
        accountsField.setAccessible(true);
        return (Map<String, List<Transaction>>) accountsField.get(null);
    }

    @SuppressWarnings("unchecked")
    private List<InterestRule> getInterestRules() throws Exception {
        Field rulesField = BankAccountInterest.class.getDeclaredField("interestRules");
        rulesField.setAccessible(true);
        return (List<InterestRule>) rulesField.get(null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getTransactionCounts() throws Exception {
        Field countsField = BankAccountInterest.class.getDeclaredField("transactionCountByDate");
        countsField.setAccessible(true);
        return (Map<String, Integer>) countsField.get(null);
    }

    private void setScanner(Scanner mockScanner) throws Exception {
        Field scannerField = BankAccountInterest.class.getDeclaredField("scanner");
        scannerField.setAccessible(true);

        // Need to get the modifiers field to remove final modifier
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);

        // Remove final modifier
        modifiersField.setInt(scannerField, scannerField.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

        // Set the mock scanner
        scannerField.set(null, mockScanner);
    }

    @BeforeEach
    public void setUpStreams() {
        outContent.reset();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @AfterEach
    public void clearStaticData() throws Exception {
        getAccounts().clear();
        getInterestRules().clear();
        getTransactionCounts().clear();

        // Add default interest rule back
        getInterestRules().add(new InterestRule(
                LocalDate.of(2023, 1, 1), "RULE01", new BigDecimal("1.95")));
    }

    @Test
    public void testMainMenuDisplay() {
        // Arrange
        String input = "Q\n"; // Simulate quitting immediately
        Scanner mockScanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        BankAccountInterest.setScanner(mockScanner); // Inject mock scanner

        // Act
        BankAccountInterest.main(new String[]{});

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Welcome to AwesomeGIC Bank! What would you like to do?"));
        assertTrue(output.contains("[T] Input Transactions"));
        assertTrue(output.contains("[I] Define Interest Rules"));
        assertTrue(output.contains("[P] Print Statement"));
        assertTrue(output.contains("[Q] Quit"));
    }

    @Test
    public void testTransactionRecordCreation() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 5, 15);
        String accountId = "ACC123";
        char type = 'D';
        BigDecimal amount = new BigDecimal("500.00");
        String transactionId = "TRX001";

        // Act
        Transaction transaction = new Transaction(date, accountId, type, amount, transactionId);

        // Assert
        assertEquals(date, transaction.date());
        assertEquals(accountId, transaction.accountId());
        assertEquals(type, transaction.type());
        assertEquals(amount, transaction.amount());
        assertEquals(transactionId, transaction.transactionId());
    }

    @Test
    public void testInterestRuleRecordCreation() {
        // Arrange
        LocalDate effectiveDate = LocalDate.of(2023, 6, 1);
        String ruleId = "RULE02";
        BigDecimal rate = new BigDecimal("2.50");

        // Act
        InterestRule rule = new InterestRule(effectiveDate, ruleId, rate);

        // Assert
        assertEquals(effectiveDate, rule.effectiveDate());
        assertEquals(ruleId, rule.ruleId());
        assertEquals(rate, rule.rate());
    }

    @Test
    public void testInterestRuleComparison() {
        // Arrange
        InterestRule rule1 = new InterestRule(LocalDate.of(2023, 1, 1), "RULE01", new BigDecimal("1.95"));
        InterestRule rule2 = new InterestRule(LocalDate.of(2023, 6, 1), "RULE02", new BigDecimal("2.50"));

        // Act & Assert
        assertTrue(rule1.compareTo(rule2) < 0);
        assertTrue(rule2.compareTo(rule1) > 0);
        assertEquals(0, rule1.compareTo(rule1));
    }

    @Test
    public void testDefaultInterestRuleExists() throws Exception {
        // Act
        List<InterestRule> rules = getInterestRules();

        // Assert
        assertFalse(rules.isEmpty());
        InterestRule defaultRule = rules.getFirst();
        assertEquals(LocalDate.of(2023, 1, 1), defaultRule.effectiveDate());
        assertEquals("RULE01", defaultRule.ruleId());
        assertEquals(new BigDecimal("1.95"), defaultRule.rate());
    }

    @Test
    public void testApplicationFlow() {
        // This test simulates a complete application flow using mocks
        String input = "T\nI\nP\nQ\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        try (MockedStatic<InputTransaction> mockedInputTransaction = Mockito.mockStatic(InputTransaction.class);
             MockedStatic<DefineInterestRules> mockedInterestRules = Mockito.mockStatic(DefineInterestRules.class);
             MockedStatic<PrintStatement> mockedPrintStatement = Mockito.mockStatic(PrintStatement.class)) {

            mockedInputTransaction.when(() -> InputTransaction.handleTransactionInput(any(), any(), any()))
                    .thenAnswer(invocation -> null);
            mockedInterestRules.when(() -> DefineInterestRules.handleInterestRuleInput(any(), any()))
                    .thenAnswer(invocation -> null);
            mockedPrintStatement.when(() -> PrintStatement.handleStatementPrint(any(), any(), any()))
                    .thenAnswer(invocation -> null);

            // Act
            BankAccountInterest.main(new String[]{});

            // Assert
            String output = outContent.toString();
            assertTrue(output.contains("Welcome to AwesomeGIC Bank!"));
            assertTrue(output.contains("Thank you for banking with AwesomeGIC Bank."));

            // Verify all methods were called
            mockedInputTransaction.verify(() -> InputTransaction.handleTransactionInput(any(), any(), any()));
            mockedInterestRules.verify(() -> DefineInterestRules.handleInterestRuleInput(any(), any()));
            mockedPrintStatement.verify(() -> PrintStatement.handleStatementPrint(any(), any(), any()));
        }
    }

    @Test
    public void testAddingTransaction() throws Exception {
        // Arrange
        Map<String, List<Transaction>> accounts = getAccounts();
        Map<String, Integer> transactionCounts = getTransactionCounts();

        LocalDate date = LocalDate.of(2023, 5, 15);
        String accountId = "ACC123";
        char type = 'D';
        BigDecimal amount = new BigDecimal("500.00");
        String transactionId = "TRX001";

        Transaction transaction = new Transaction(date, accountId, type, amount, transactionId);

        // Act
        accounts.computeIfAbsent(accountId, k -> new ArrayList<>()).add(transaction);
        transactionCounts.put(date.toString(), 1);

        // Assert
        assertTrue(accounts.containsKey(accountId));
        assertEquals(1, accounts.get(accountId).size());
        assertEquals(transaction, accounts.get(accountId).getFirst());
        assertTrue(transactionCounts.containsKey(date.toString()));
        assertEquals(1, transactionCounts.get(date.toString()).intValue());
    }

    @Test
    public void testAddingMultipleTransactions() throws Exception {
        // Arrange
        Map<String, List<Transaction>> accounts = getAccounts();
        Map<String, Integer> transactionCounts = getTransactionCounts();

        String accountId = "ACC123";
        LocalDate date1 = LocalDate.of(2023, 5, 15);
        LocalDate date2 = LocalDate.of(2023, 5, 16);

        Transaction transaction1 = new Transaction(date1, accountId, 'D', new BigDecimal("500.00"), "TRX001");
        Transaction transaction2 = new Transaction(date2, accountId, 'W', new BigDecimal("200.00"), "TRX002");

        // Act
        accounts.computeIfAbsent(accountId, k -> new ArrayList<>()).add(transaction1);
        accounts.get(accountId).add(transaction2);

        transactionCounts.put(date1.toString(), 1);
        transactionCounts.put(date2.toString(), 1);

        // Assert
        assertEquals(2, accounts.get(accountId).size());
        assertEquals(transaction1, accounts.get(accountId).get(0));
        assertEquals(transaction2, accounts.get(accountId).get(1));
        assertEquals(1, transactionCounts.get(date1.toString()).intValue());
        assertEquals(1, transactionCounts.get(date2.toString()).intValue());
    }

    @Test
    public void testAddingInterestRule() throws Exception {
        // Arrange
        List<InterestRule> rules = getInterestRules();
        int initialSize = rules.size();

        LocalDate newDate = LocalDate.of(2023, 7, 1);
        String newRuleId = "RULE02";
        BigDecimal newRate = new BigDecimal("2.75");

        InterestRule newRule = new InterestRule(newDate, newRuleId, newRate);

        // Act
        rules.add(newRule);

        // Assert
        assertEquals(initialSize + 1, rules.size());
        assertTrue(rules.contains(newRule));
    }

    @Test
    public void testFindingApplicableInterestRate() throws Exception {
        // Arrange
        List<InterestRule> rules = getInterestRules();

        // Add additional rules
        rules.add(new InterestRule(
                LocalDate.of(2023, 4, 1), "RULE02", new BigDecimal("2.15")));
        rules.add(new InterestRule(
                LocalDate.of(2023, 7, 1), "RULE03", new BigDecimal("2.35")));

        // Sort rules by date
        rules.sort(InterestRule::compareTo);

        // Act - Find applicable rule for different dates
        InterestRule ruleForMarch = findApplicableRule(rules, LocalDate.of(2023, 3, 15));
        InterestRule ruleForMay = findApplicableRule(rules, LocalDate.of(2023, 5, 15));
        InterestRule ruleForAugust = findApplicableRule(rules, LocalDate.of(2023, 8, 10));

        // Assert
        assertEquals("RULE01", ruleForMarch.ruleId());
        assertEquals(new BigDecimal("1.95"), ruleForMarch.rate());

        assertEquals("RULE02", ruleForMay.ruleId());
        assertEquals(new BigDecimal("2.15"), ruleForMay.rate());

        assertEquals("RULE03", ruleForAugust.ruleId());
        assertEquals(new BigDecimal("2.35"), ruleForAugust.rate());
    }

    @Test
    public void testCalculateInterestForAccount() throws Exception {
        // Arrange
        Map<String, List<Transaction>> accounts = getAccounts();
        List<InterestRule> rules = getInterestRules();

        String accountId = "ACC123";
        LocalDate date1 = LocalDate.of(2023, 5, 15);

        // Add a transaction
        Transaction deposit = new Transaction(date1, accountId, 'D',
                new BigDecimal("1000.00"), "TRX001");

        accounts.computeIfAbsent(accountId, k -> new ArrayList<>()).add(deposit);

        // Calculate interest for 30 days at 1.95% annual rate
        BigDecimal dailyRate = new BigDecimal("1.95").divide(new BigDecimal("36500"), 2, RoundingMode.HALF_UP);
        BigDecimal expectedInterest = new BigDecimal("1000.00")
                .multiply(dailyRate)
                .multiply(new BigDecimal("30"))
                .setScale(2, RoundingMode.HALF_UP);

        // Act
        BigDecimal calculatedInterest = calculateInterestForPeriod(
                accounts.get(accountId), rules, date1, date1.plusDays(30));

        // Assert
        assertEquals(expectedInterest, calculatedInterest);
    }

    @Test
    public void testCalculateBalanceWithInterest() throws Exception {
        // Arrange
        Map<String, List<Transaction>> accounts = getAccounts();
        List<InterestRule> rules = getInterestRules();

        String accountId = "ACC123";
        LocalDate startDate = LocalDate.of(2023, 5, 1);
        LocalDate depositDate = LocalDate.of(2023, 5, 5);
        LocalDate withdrawalDate = LocalDate.of(2023, 5, 20);
        LocalDate endDate = LocalDate.of(2023, 5, 31);

        // Add transactions
        Transaction deposit = new Transaction
                (depositDate, accountId, 'D', new BigDecimal("1000.00"), "TRX001");
        Transaction withdrawal = new Transaction
                (withdrawalDate, accountId, 'W', new BigDecimal("300.00"), "TRX002");

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(deposit);
        transactions.add(withdrawal);
        accounts.put(accountId, transactions);

        // Act
        BigDecimal finalBalance = calculateBalanceWithInterest(accountId, accounts, rules, startDate, endDate);
        System.out.println("Final Balance: " + finalBalance);

        // Assert - balance should be 1000 - 300 + interest
        assertTrue(finalBalance.compareTo(new BigDecimal("700.00")) >= 0);
    }

    // Helper method to find the applicable interest rule for a given date
    private InterestRule findApplicableRule(
            List<InterestRule> rules, LocalDate date) {

        InterestRule applicableRule = null;

        for (InterestRule rule : rules) {
            if (!rule.effectiveDate().isAfter(date) &&
                    (applicableRule == null || rule.effectiveDate().isAfter(applicableRule.effectiveDate()))) {
                applicableRule = rule;
            }
        }

        return applicableRule;
    }

    // Helper method to calculate interest for a period
    private BigDecimal calculateInterestForPeriod(
            List<Transaction> transactions,
            List<InterestRule> rules,
            LocalDate startDate,
            LocalDate endDate) {

        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal interest = BigDecimal.ZERO;
        LocalDate currentDate = startDate;

        // Sort transactions by date
        transactions.sort((t1, t2) -> t1.date().compareTo(t2.date()));

        // Sort rules by effective date
        rules.sort(InterestRule::compareTo);

        int transactionIndex = 0;

        while (!currentDate.isAfter(endDate)) {
            // Process transactions for the current date
            while (transactionIndex < transactions.size() &&
                    !transactions.get(transactionIndex).date().isAfter(currentDate)) {

                Transaction transaction = transactions.get(transactionIndex);
                if (transaction.type() == 'D') {
                    balance = balance.add(transaction.amount());
                } else if (transaction.type() == 'W') {
                    balance = balance.subtract(transaction.amount());
                }

                transactionIndex++;
            }

            // Find applicable interest rate
            InterestRule applicableRule = findApplicableRule(rules, currentDate);

            if (applicableRule != null && balance.compareTo(BigDecimal.ZERO) > 0) {
                // Calculate daily interest
                BigDecimal dailyRate = applicableRule.rate()
                        .divide(new BigDecimal("36500"), 2, RoundingMode.HALF_UP);
                BigDecimal dailyInterest = balance.multiply(dailyRate);

                interest = interest.add(dailyInterest);
            }

            currentDate = currentDate.plusDays(1);
        }

        return interest.setScale(2, RoundingMode.HALF_UP);
    }

    // Helper method to calculate final balance with interest
    private BigDecimal calculateBalanceWithInterest(
            String accountId,
            Map<String, List<Transaction>> accounts,
            List<InterestRule> rules,
            LocalDate startDate,
            LocalDate endDate) {

        if (!accounts.containsKey(accountId)) {
            return BigDecimal.ZERO;
        }

        List<Transaction> transactions = accounts.get(accountId);
        BigDecimal balance = BigDecimal.ZERO;

        // Calculate final balance from transactions
        for (Transaction transaction : transactions) {
            if (!transaction.date().isBefore(startDate) && !transaction.date().isAfter(endDate)) {
                if (transaction.type() == 'D') {
                    balance = balance.add(transaction.amount());
                } else if (transaction.type() == 'W') {
                    balance = balance.subtract(transaction.amount());
                }
            }
        }

        // Add interest
        BigDecimal interest = calculateInterestForPeriod(transactions, rules, startDate, endDate);
        return balance.add(interest);
    }
}