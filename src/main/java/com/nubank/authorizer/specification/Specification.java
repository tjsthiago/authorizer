package com.nubank.authorizer.specification;

import java.util.List;

import com.nubank.operations.Operation;

public interface Specification {
	
	public void aplyValidations(List<Operation> operations);

}
