package com.nubank.authorizer.specification;

import java.util.List;

import com.nubank.operations.Operation;

public interface Specification {
	
	public void applyValidatons(List<Operation> operations);

}
