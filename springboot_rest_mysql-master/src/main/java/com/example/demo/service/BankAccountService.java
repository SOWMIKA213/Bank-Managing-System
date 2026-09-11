package com.example.demo.service;

import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.dto.MoneyRequest;
import com.example.demo.dto.TransferRequest;
import com.example.demo.entity.BankAccount;
import com.example.demo.entity.Transaction;

import java.util.List;

public interface BankAccountService {

    BankAccount createAccount(CreateAccountRequest request);

    BankAccount getAccount(String accountNumber);

    List<BankAccount> getAllAccounts();

    BankAccount deposit(String accountNumber, MoneyRequest request);

    BankAccount withdraw(String accountNumber, MoneyRequest request);

    String transfer(TransferRequest request);

    List<Transaction> getTransactionHistory(String accountNumber);
}