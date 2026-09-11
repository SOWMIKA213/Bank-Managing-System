package com.example.demo.service.impl;

import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.dto.MoneyRequest;
import com.example.demo.dto.TransferRequest;
import com.example.demo.entity.BankAccount;
import com.example.demo.entity.Transaction;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.service.BankAccountService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository accountRepo;
    private final TransactionRepository transactionRepo;

    public BankAccountServiceImpl(
            BankAccountRepository accountRepo,
            TransactionRepository transactionRepo) {

        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
    }

    // =========================
    // CREATE ACCOUNT
    // =========================

    @Override
    public BankAccount createAccount(CreateAccountRequest request) {

        // Validate name
        if (request.getName() == null ||
                request.getName().trim().isEmpty()) {

            throw new IllegalArgumentException("Name is required");
        }

        // Validate age
        if (request.getAge() <= 0) {

            throw new IllegalArgumentException("Invalid age");
        }

        // Validate PIN
        if (request.getPin() == null ||
                !request.getPin().matches("\\d{4}")) {

            throw new IllegalArgumentException(
                    "PIN must be exactly 4 digits");
        }

        BankAccount account = new BankAccount();

        account.setName(request.getName());
        account.setAge(request.getAge());
        account.setPin(request.getPin());

        // =========================
        // AGE BASED ACCOUNT
        // =========================

        if (request.getAge() < 18) {

            // Below 18
            account.setAccountType("MINOR/STUDENT");
            account.setBalance(0);

        } else {

            // 18 or above
            if (request.getAccountType() == null ||
                    (!request.getAccountType().equalsIgnoreCase("SAVING")
                            && !request.getAccountType().equalsIgnoreCase("CURRENT"))) {

                throw new IllegalArgumentException(
                        "Major account must be SAVING or CURRENT");
            }

            if (request.getAccountType().equalsIgnoreCase("SAVING")) {

                account.setAccountType("MAJOR-SAVING");
                account.setBalance(10000);

            } else {

                account.setAccountType("MAJOR-CURRENT");
                account.setBalance(20000);
            }
        }

        // =========================
        // GENERATE SBI ACCOUNT NUMBER
        // =========================

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();

        } while (accountRepo.existsByAccountNumber(accountNumber));

        account.setAccountNumber(accountNumber);

        // Save account
        BankAccount savedAccount = accountRepo.save(account);

        // Add opening transaction
        addTransaction(
                accountNumber,
                "ACCOUNT_OPENED",
                savedAccount.getBalance(),
                savedAccount.getBalance(),
                "Account opened"
        );

        return savedAccount;
    }

    // =========================
    // ACCOUNT NUMBER GENERATOR
    // =========================

    private String generateAccountNumber() {

        Random random = new Random();

        int number = 100000 + random.nextInt(900000);

        return "SBI00" + number;
    }

    // =========================
    // GET ACCOUNT
    // =========================

    @Override
    public BankAccount getAccount(String accountNumber) {

        return accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found " + accountNumber));
    }

    // =========================
    // GET ALL ACCOUNTS
    // =========================

    @Override
    public List<BankAccount> getAllAccounts() {

        return accountRepo.findAll();
    }

    // =========================
    // DEPOSIT
    // =========================

    @Override
    public BankAccount deposit(
            String accountNumber,
            MoneyRequest request) {

        BankAccount account = getAccount(accountNumber);

        // Validate amount
        validateAmount(request.getAmount());

        // Verify PIN
        verifyPin(account, request.getPin());

        // Add money
        account.setBalance(
                account.getBalance() + request.getAmount());

        BankAccount saved = accountRepo.save(account);

        // Save transaction
        addTransaction(
                accountNumber,
                "DEPOSIT",
                request.getAmount(),
                saved.getBalance(),
                "Deposit money"
        );

        return saved;
    }

    // =========================
    // WITHDRAW
    // =========================

    @Override
    public BankAccount withdraw(
            String accountNumber,
            MoneyRequest request) {

        BankAccount account = getAccount(accountNumber);

        // Validate amount
        validateAmount(request.getAmount());

        // Check balance
        if (account.getBalance() < request.getAmount()) {

            throw new IllegalArgumentException(
                    "Insufficient funds");
        }

        // Verify PIN
        verifyPin(account, request.getPin());

        // Deduct money
        account.setBalance(
                account.getBalance() - request.getAmount());

        BankAccount saved = accountRepo.save(account);

        // Save transaction
        addTransaction(
                accountNumber,
                "WITHDRAW",
                request.getAmount(),
                saved.getBalance(),
                "Withdraw money"
        );

        return saved;
    }

    // =========================
    // TRANSFER
    // =========================

    @Override
    @Transactional
    public String transfer(TransferRequest request) {

        // Check account numbers
        if (request.getFromAccount() == null ||
                request.getToAccount() == null) {

            throw new IllegalArgumentException(
                    "Source and recipient accounts are required");
        }

        // Same account check
        if (request.getFromAccount()
                .equalsIgnoreCase(request.getToAccount())) {

            throw new IllegalArgumentException(
                    "Cannot transfer to the same account");
        }

        // Validate amount
        validateAmount(request.getAmount());

        // Get sender
        BankAccount sender =
                getAccount(request.getFromAccount());

        // Get receiver
        BankAccount receiver =
                getAccount(request.getToAccount());

        // Check balance
        if (sender.getBalance() < request.getAmount()) {

            throw new IllegalArgumentException(
                    "Insufficient funds");
        }

        // Verify sender PIN
        verifyPin(sender, request.getPin());

        // Deduct from sender
        sender.setBalance(
                sender.getBalance() - request.getAmount());

        // Add to receiver
        receiver.setBalance(
                receiver.getBalance() + request.getAmount());

        // Save both accounts
        accountRepo.save(sender);
        accountRepo.save(receiver);

        // Sender transaction
        addTransaction(
                sender.getAccountNumber(),
                "TRANSFER",
                request.getAmount(),
                sender.getBalance(),
                "Transfer to " + receiver.getAccountNumber()
        );

        // Receiver transaction
        addTransaction(
                receiver.getAccountNumber(),
                "TRANSFER",
                request.getAmount(),
                receiver.getBalance(),
                "Transfer from " + sender.getAccountNumber()
        );

        return "Rs." + request.getAmount()
                + " successfully transferred from "
                + sender.getAccountNumber()
                + " to "
                + receiver.getAccountNumber();
    }

    // =========================
    // TRANSACTION HISTORY
    // =========================

    @Override
    public List<Transaction> getTransactionHistory(
            String accountNumber) {

        // Check account exists
        getAccount(accountNumber);

        return transactionRepo
                .findByAccountNumberOrderByTransactionTimeDesc(
                        accountNumber);
    }

    // =========================
    // VALIDATE AMOUNT
    // =========================

    private void validateAmount(double amount) {

        if (amount <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
    }

    // =========================
    // VERIFY PIN
    // =========================

    private void verifyPin(
            BankAccount account,
            String inputPin) {

        if (inputPin == null ||
                !account.getPin().equals(inputPin)) {

            throw new IllegalArgumentException(
                    "Incorrect PIN");
        }
    }

    // =========================
    // ADD TRANSACTION
    // =========================

    private void addTransaction(
            String accountNumber,
            String type,
            double amount,
            double balanceAfter,
            String description) {

        Transaction transaction = new Transaction();

        transaction.setAccountNumber(accountNumber);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setTransactionTime(LocalDateTime.now());

        transactionRepo.save(transaction);
    }
}