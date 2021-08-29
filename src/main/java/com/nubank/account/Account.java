package com.nubank.account;

import java.util.List;

import com.nubank.operations.transaction.Transaction;

public class Account {
	private boolean activeCard;
	private int availableLimit;

	public void initializeAccount(boolean activeCard, int availableLimit) {
		this.activeCard = activeCard;
		this.availableLimit = availableLimit;
	}
	
	public void processTransaction(Transaction transaction) {
		this.availableLimit = this.getAvailableLimit() - transaction.getAmount(); 
	}
	
	public void showState() {
		System.out.println(
			String.format(
				"{\"account\": {\"active-card\": %b, \"available-limit\": %d}, \"violations\": []}", 
				this.isActiveCard(), 
				this.getAvailableLimit()
			)
		);
	}
	
	public void showStateWithViolations(List<String> violations) {
		System.out.println(
			String.format(
				"{\"account\": {\"active-card\": %b, \"available-limit\": %d}, \"violations\": %s}", 
				this.isActiveCard(), 
				this.getAvailableLimit(),
				violations.toString()
			)
		);
	}
	
	public boolean isActiveCard() {
		return activeCard;
	}

	public int getAvailableLimit() {
		return availableLimit;
	}
	
}
