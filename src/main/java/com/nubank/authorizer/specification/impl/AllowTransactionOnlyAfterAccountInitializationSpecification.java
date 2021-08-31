package com.nubank.authorizer.specification.impl;

import java.util.List;

import com.nubank.authorizer.specification.Specification;
import com.nubank.operations.CreateAccount;
import com.nubank.operations.Operation;
import com.nubank.operations.Transaction;

public class AllowTransactionOnlyAfterAccountInitializationSpecification implements Specification {

	@Override
	public void applyValidatons(List<Operation> operations) {
		
		boolean accountInitialized = false;

		for (Operation operation : operations) {
			if (operation instanceof CreateAccount) {
				accountInitialized = true;
			}

			if (operation instanceof Transaction && !accountInitialized) {
				operation.addViolation(operation, "account-not-initialized");
			}

		}
		
	}

}
