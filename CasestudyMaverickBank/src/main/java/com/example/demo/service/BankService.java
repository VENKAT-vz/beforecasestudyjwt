package com.example.demo.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Account;
import com.example.demo.domain.Transaction;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;

@Service
public class BankService {
	
    @Autowired
    private AccountRepository accountrepo;

    @Autowired
    private TransactionRepository transactionRepository; 

    @Transactional
    public String deposit(String accountNumber, double amount) {
    	accountrepo.depositAmount(accountNumber, amount);

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setAmount(amount);
        transaction.setTransactionDate(new java.sql.Date(new Date().getTime()));
        transaction.setTransactionType("deposit");
        transaction.setDescription("Deposit into account");

        transactionRepository.save(transaction);

        return "Amount deposited successfully.";
    }

    @Transactional
    public String withdraw(String accountNumber, double amount) {
        double currentBalance = getCurrentBalance(accountNumber);
        if (currentBalance < amount) {
            return "Insufficient balance.";
        }

        accountrepo.withdrawAmount(accountNumber, amount);

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setAmount(amount);
        transaction.setTransactionDate(new java.sql.Date(new Date().getTime()));
        transaction.setTransactionType("withdrawal");
        transaction.setDescription("Withdrawal from account");

        transactionRepository.save(transaction);

        return "Amount withdrawn successfully.";
    }
    
    
    @Transactional
    public String accountTransfer(String accountnumber1, String accountnumber2, double amount) {
        String senderAccountNumber = accountnumber1;
        String receiverAccountNumber = accountnumber2;

        double senderBalance = getCurrentBalance(senderAccountNumber);
        if (senderBalance < amount) {
            return "Insufficient balance.";
        }

        accountrepo.withdrawAmount(senderAccountNumber, amount);
        accountrepo.depositAmount(receiverAccountNumber, amount);

        Transaction senderTransaction = new Transaction();
        senderTransaction.setAccountNumber(senderAccountNumber);
        senderTransaction.setAmount(amount);
        senderTransaction.setTransactionDate(new java.sql.Date(new Date().getTime()));
        senderTransaction.setTransactionType("transfer-out");
        senderTransaction.setDescription("Transfer to " + receiverAccountNumber);
        transactionRepository.save(senderTransaction);

        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setAccountNumber(receiverAccountNumber);
        receiverTransaction.setAmount(amount);
        receiverTransaction.setTransactionDate(new java.sql.Date(new Date().getTime()));
        receiverTransaction.setTransactionType("transfer-in");
        receiverTransaction.setDescription("Transfer from " + senderAccountNumber);
        transactionRepository.save(receiverTransaction);

        return "Transfer successful: " + amount + " transferred from " + senderAccountNumber + " to " + receiverAccountNumber;
    }
    
    @Transactional
    public String transferByContactNumber(String senderContactNumber, String receiverContactNumber, double amount) {
        Account senderAccount = findAccountForTransfer(senderContactNumber);
        Account receiverAccount = findAccountForTransfer(receiverContactNumber);

        if (senderAccount == null || receiverAccount == null) {
            return "Either sender or receiver has no eligible account for transfer";
        }

        if (senderAccount.getBalance() < amount) {
            return "Insufficient funds";
        }

        String senderAccountNumber=senderAccount.getAccountNumber();
        String receiverAccountNumber=receiverAccount.getAccountNumber();
        accountrepo.withdrawAmount(senderAccountNumber, amount);
        accountrepo.depositAmount(receiverAccountNumber, amount);

        Transaction senderTransaction = new Transaction();
        senderTransaction.setAccountNumber(senderAccountNumber);
        senderTransaction.setAmount(amount);
        senderTransaction.setTransactionDate(new java.sql.Date(new Date().getTime()));
        senderTransaction.setTransactionType("transfer-out");
        senderTransaction.setDescription("Transfer to " + receiverAccountNumber);
        transactionRepository.save(senderTransaction);

        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setAccountNumber(receiverAccountNumber);
        receiverTransaction.setAmount(amount);
        receiverTransaction.setTransactionDate(new java.sql.Date(new Date().getTime()));
        receiverTransaction.setTransactionType("transfer-in");
        receiverTransaction.setDescription("Transfer from " + senderAccountNumber);
        transactionRepository.save(receiverTransaction);
        return "Amount of " + amount +" transferred from "+senderAccount.getAccountNumber()+" to "+
        receiverAccount.getAccountNumber();
    }
    
	public Account findAccountForTransfer(String contactNumber) {
	    List<Account> activeAccounts = accountrepo.findActiveAccountsByContactNumber(contactNumber);
	    
	    if (activeAccounts.size() == 1) {
	        return activeAccounts.get(0); 
	    } else if (activeAccounts.size() > 1) {
	        return activeAccounts.stream()
	                .filter(account -> account.getAccountType().equalsIgnoreCase("Savings"))
	                .findFirst()
	                .orElse(activeAccounts.get(0)); 
	    }
	    return null; 
	}


    public double getCurrentBalance(String accountNumber) {
        return accountrepo.findBalanceByAccountNumber(accountNumber);
    }

    public double getCurrentBalanceByUsername(String username) {
        return accountrepo.findBalanceByUsername(username);
    }
    
}
