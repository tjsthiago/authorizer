package com.nubank.authorizer.specification.impl;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.nubank.authorizer.specification.Specification;
import com.nubank.operations.Operation;
import com.nubank.operations.transaction.Transaction;
import com.nubank.parser.DateUtils;

public class DoubledTransactionSpecification implements Specification{

	@Override
	public void applyValidatons(List<Operation> operations) {
		
		List<Transaction> transactions = getTransactionOerations(operations);
		
		for (Transaction currentTransaction : transactions) {
			
			List<Transaction> transactionsOccurredTwoMinutesAgoOfCurrentTransaction = getTransactionsOccurredTwoMinutesAgoOfCurrentTransaction(currentTransaction, transactions);
			
			if(thereIsDoubleTransaction(currentTransaction, transactionsOccurredTwoMinutesAgoOfCurrentTransaction)) {
				currentTransaction.addViolation(currentTransaction, "doubled-transaction");
			}
			
		}
		
	}

	private boolean thereIsDoubleTransaction(Transaction currentTransaction, List<Transaction> transactionsOccurredTwoMinutesAgoOfCurrentTransaction) {
		return transactionsOccurredTwoMinutesAgoOfCurrentTransaction
				.stream()
				.anyMatch(
					t -> t.getMerchant().equals(currentTransaction.getMerchant()) && 
					t.getAmount() == currentTransaction.getAmount()
				);
	}

	private List<Transaction> getTransactionsOccurredTwoMinutesAgoOfCurrentTransaction(Transaction currentTransaction, List<Transaction> transactions) {
		Date currentTransactionTime = currentTransaction.getTime();
		long doubleTransactionTimeWindowInSeconds = 120;
		
		return transactions
				.stream()
				.filter(isBeforeCurrentTransaction(currentTransaction))
				.filter(t -> getDiferenceInSecondsBetweenTransactions(t.getTime(), currentTransactionTime) < doubleTransactionTimeWindowInSeconds)
				.collect(Collectors.toList());
	}
	
	private Predicate<? super Transaction> isBeforeCurrentTransaction(Transaction currentTransaction) {
		return t -> currentTransaction.getTime().compareTo(t.getTime()) > 0;
	}
	
	private long getDiferenceInSecondsBetweenTransactions(Date previousTransactionTime, Date currentTransactionTime) {
		return DateUtils.getDiferenceInSecondsBetweenTwoDates(previousTransactionTime, currentTransactionTime);
	}

	private List<Transaction> getTransactionOerations(List<Operation> operations) {
		return operations.stream().filter(isTransactionOperation()).map(o -> (Transaction) o).collect(Collectors.toList());
	}
	
	private Predicate<? super Operation> isTransactionOperation() {
		return o -> o instanceof Transaction;
	}
	
}
