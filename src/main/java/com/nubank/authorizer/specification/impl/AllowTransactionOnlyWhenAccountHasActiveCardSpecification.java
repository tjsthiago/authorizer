package com.nubank.authorizer.specification.impl;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.nubank.authorizer.specification.Specification;
import com.nubank.operations.CreateAccount;
import com.nubank.operations.Operation;
import com.nubank.operations.Transaction;

public class AllowTransactionOnlyWhenAccountHasActiveCardSpecification implements Specification {

	@Override
	public void applyValidatons(List<Operation> operations) {
		Optional<Operation> createAccountOperation = getAccountCreationOperation(operations);
		
		if(createAccountOperation.isPresent()) {
			CreateAccount createAccount = (CreateAccount) createAccountOperation.get();
			
			if (hasDisableCard(createAccount)) {
				addViolationsToAllTransactions(operations);
			}
		}
		
	}

	private void addViolationsToAllTransactions(List<Operation> operations) {
		operations.stream().filter(isTransactionOperation()).forEach(o -> o.addViolation(o, "card-not-active"));
	}

	private boolean hasDisableCard(CreateAccount createAccountOperation) {
		return !createAccountOperation.isActiveCard();
	}

	private Optional<Operation> getAccountCreationOperation(List<Operation> operations) {
		return operations.stream().filter(isCreateAccountingOperation()).findFirst();
	}

	private Predicate<? super Operation> isCreateAccountingOperation() {
		return o -> o instanceof CreateAccount;
	}
	
	private Predicate<? super Operation> isTransactionOperation() {
		return o -> o instanceof Transaction;
	}

}
