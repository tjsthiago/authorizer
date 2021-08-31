package com.nubank.authorizer;

import java.util.List;
import java.util.Optional;

import com.nubank.account.Account;
import com.nubank.authorizer.specification.SpecificationsBuilder;
import com.nubank.operations.CreateAccount;
import com.nubank.operations.Operation;
import com.nubank.operations.Transaction;

public class Authorizer {
	private Account account;
	private SpecificationsBuilder specificationConfiguration;
	
	public Authorizer(SpecificationsBuilder specificationConfiguration, Account account) {
		this.specificationConfiguration = specificationConfiguration;
		this.account = account;
	}

	public void applyValidations(List<Operation> operations) {
		specificationConfiguration
			.getOperationsSpecifications()
			.stream()
			.forEach(s -> s.applyValidatons(operations));
		
		processOperations(operations);
		
	}
	
	private void processOperations(List<Operation> operations) {
		for (Operation operation : operations) {
			
			if(thereIsViolations(operation)) {
				
				Optional<String> accountNotInitializedViolation = getViolationByRestriction(operations.get(0), "account-not-initialized");
				
				if(accountNotInitializedViolation.isPresent()) {
					System.out.println(
						String.format(
							"{\"account\": {}, \"violations\": %s}", 
							operation.getViolations().toString()
						)
					);
				}else {
					account.showStateWithViolations(operation.getViolations());
				}
				
			}else {
				
				if (operation instanceof CreateAccount) {
					CreateAccount createAccount = (CreateAccount) operation;
					account.initializeAccount(createAccount.isActiveCard(), createAccount.getAvailableLimit());
					account.showState();
				}
				
				if (operation instanceof Transaction) {
					Transaction transaction = (Transaction) operation;
					account.processTransaction(transaction);
					account.showState();
				}
				
			}
			
		}
	}
	
	private Optional<String> getViolationByRestriction(Operation operation, String restriction) {
		return operation.getViolations().stream().filter(v -> v.equalsIgnoreCase(restriction)).findFirst();
	}

	private boolean thereIsViolations(Operation operation) {
		return !operation.getViolations().isEmpty();
	}

}
