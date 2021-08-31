package com.nubank.authorizer.specification.impl;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.nubank.authorizer.specification.Specification;
import com.nubank.operations.Operation;
import com.nubank.operations.transaction.Transaction;
import com.nubank.parser.DateUtils;

public class HighFrequencySmalIntervalSpecification implements Specification {

	@Override
	public void applyValidatons(List<Operation> operations) {
		int allowedHighFrequencySmalIntervalOperations = 3;
		int highFrequencySmalIntervalOperations = 1;
		long minimumDifferenceBetweenTransactionsInSeconds = 120;
		
		List<Transaction> transactions = getTransactionOerations(operations);
		
		Date previousTransactionTime = null;
		
		for (int i = 0; i < transactions.size(); i++) {
			if(thereIsPreviousTransaction(i)) {
				Transaction currentTransaction = transactions.get(i);
				
				previousTransactionTime = getPreviousTransactionTime(transactions, i);
				Date currentTransactionTime = currentTransaction.getTime();
				
				long diferenceInSecondsBetweenTwoDates = getDiferenceInSecondsBetweenTransactions(
					previousTransactionTime, 
					currentTransactionTime
				);
				
				if(minimumDifferenceBetweenTransactionsInSecondsViolated(minimumDifferenceBetweenTransactionsInSeconds, diferenceInSecondsBetweenTwoDates)) {
					highFrequencySmalIntervalOperations ++;
				}else {
					highFrequencySmalIntervalOperations = 0;
				}
				
				if(allowedHighFrequencySmalIntervalOperationsExceded(allowedHighFrequencySmalIntervalOperations, highFrequencySmalIntervalOperations)) {
					currentTransaction.addViolation(currentTransaction, "high-frequency-small-interval");
				}
				
			}
		}
		
	}

	private boolean minimumDifferenceBetweenTransactionsInSecondsViolated(long minimumDifferenceBetweenTransactionsInSeconds, long diferenceInSecondsBetweenTwoDates) {
		return diferenceInSecondsBetweenTwoDates < minimumDifferenceBetweenTransactionsInSeconds;
	}

	private boolean allowedHighFrequencySmalIntervalOperationsExceded(int allowedHighFrequencySmalIntervalOperations, int highFrequencySmalIntervalOperations) {
		return highFrequencySmalIntervalOperations > allowedHighFrequencySmalIntervalOperations;
	}

	private long getDiferenceInSecondsBetweenTransactions(Date previousTransactionTime, Date currentTransactionTime) {
		return DateUtils.getDiferenceInSecondsBetweenTwoDates(previousTransactionTime, currentTransactionTime);
	}

	private Date getPreviousTransactionTime(List<Transaction> transactions, int transactionsIterationIndex) {
		return transactions.get(transactionsIterationIndex - 1).getTime();
	}

	private boolean thereIsPreviousTransaction(int transactionsIterationIndex) {
		return transactionsIterationIndex > 0;
	}

	private List<Transaction> getTransactionOerations(List<Operation> operations) {
		return operations.stream().filter(isTransactionOperation()).map(o -> (Transaction) o).collect(Collectors.toList());
	}
	
	private Predicate<? super Operation> isTransactionOperation() {
		return o -> o instanceof Transaction;
	}

}
