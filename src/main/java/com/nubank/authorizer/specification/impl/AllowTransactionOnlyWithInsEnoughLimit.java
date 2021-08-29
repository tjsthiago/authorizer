package com.nubank.authorizer.specification.impl;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.nubank.account.Account;
import com.nubank.authorizer.specification.Specification;
import com.nubank.operations.Operation;
import com.nubank.operations.create.account.CreateAccount;
import com.nubank.operations.transaction.Transaction;

public class AllowTransactionOnlyWithInsEnoughLimit implements Specification {

	@Override
	public void applyValidatons(List<Operation> operations) {
		Optional<Operation> createAccountOperation = getAccountCreationOperation(operations);
		
		if(createAccountOperation.isPresent()) {
			Account account = createAccountToValidateLimit(createAccountOperation.get());
			validateAvailableLimitForAllTransactions(operations, account);
		}
	}

	private void validateAvailableLimitForAllTransactions(List<Operation> operations, Account account) {
		operations.stream().filter(isTransactionOperation()).forEach(operation -> {
			
			Transaction transaction = (Transaction) operation;
			
			if(transactionExceededAccountLimit(account, transaction)) {
				operation.addViolation(operation, "insufficient-limit");
			}
			
		});
	}

	private boolean transactionExceededAccountLimit(Account account, Transaction transaction) {
		return transaction.getAmount() > account.getAvailableLimit();
	}

	private Account createAccountToValidateLimit(Operation createAccountOperation) {
		Account account = new Account();
		
		CreateAccount createAccount = (CreateAccount) createAccountOperation;
		account.initializeAccount(createAccount.isActiveCard(), createAccount.getAvailableLimit());
		
		return account;
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
