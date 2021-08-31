package com.nubank.authorizer.specification.impl;

import java.util.List;

import com.nubank.authorizer.specification.Specification;
import com.nubank.operations.CreateAccount;
import com.nubank.operations.Operation;

public class AllowOnlyOneAccountSpecification implements Specification {

	@Override
	public void applyValidatons(List<Operation> operations) {
		Long createAccountingOperations = 0L;

		for (Operation operation : operations) {
			
			if (operation instanceof CreateAccount) {
				createAccountingOperations++;
			}

			if (createAccountingOperations > 1) {
				operation.addViolation(operation, "accountalready-initialized");
			}
			
		}

	}

}
