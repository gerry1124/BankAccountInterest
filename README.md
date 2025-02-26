# BankAccountInterest


This application is built using Java 21 and IntelliJ as IDE. Main method (the primary entrypoint to the application) can be found in:

    src/main/java/org/gerry/solution/BankAccountInterest.java

Dependencies for testing (JUnit, Mockito), can be installed using Maven (assuming Apache Maven is already configured on your machine) by running the command:

    mvn clean install

Unit Tests can be found in:

    src/test/java/org/gerry/test/BankAccountInterestTest.java
You check the tests by running the command:

    mvn test


# Relevant Files

Under the main java directory, there are 3 classes that I defined to handle the requirements of the problem.

1. To handle user input for defining interest rules:
   src/main/java/org/gerry/solution/DefineInterestRules.java

2. To handle user input for selecting transaction types:
   src/main/java/org/gerry/solution/InputTransaction.java

3. And to display the bank statement and calculation of interest:
   src/main/java/org/gerry/solution/PrintStatement.java



Thanks,

Gerard
