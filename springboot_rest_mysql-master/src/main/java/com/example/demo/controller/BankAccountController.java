package com.example.demo.controller;

import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.dto.MoneyRequest;
import com.example.demo.dto.TransferRequest;
import com.example.demo.entity.BankAccount;
import com.example.demo.entity.Transaction;
import com.example.demo.service.BankAccountService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank/accounts")
public class BankAccountController {

    private final BankAccountService service;

    public BankAccountController(BankAccountService service) {
        this.service = service;
    }

    @PostMapping
    public BankAccount createAccount(
            @RequestBody CreateAccountRequest request) {

        return service.createAccount(request);
    }

    @GetMapping("/{accountNumber}")
    public BankAccount getAccount(
            @PathVariable String accountNumber) {

        return service.getAccount(accountNumber);
    }

    @GetMapping
    public List<BankAccount> getAllAccounts() {

        return service.getAllAccounts();
    }

    @PostMapping("/{accountNumber}/deposit")
    public BankAccount deposit(
            @PathVariable String accountNumber,
            @RequestBody MoneyRequest request) {

        return service.deposit(accountNumber, request);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public BankAccount withdraw(
            @PathVariable String accountNumber,
            @RequestBody MoneyRequest request) {

        return service.withdraw(accountNumber, request);
    }

    @PostMapping("/transfer")
    public String transfer(
            @RequestBody TransferRequest request) {

        return service.transfer(request);
    }

    @GetMapping("/{accountNumber}/transactions")
    public List<Transaction> transactionHistory(
            @PathVariable String accountNumber) {

        return service.getTransactionHistory(accountNumber);
    }
}