package com.nubank.authorizer;

import java.util.List;

import com.nubank.authorizer.specification.SpecificationBuilder;
import com.nubank.operations.Operation;

public class Authorizer {
	private SpecificationBuilder specificationConfiguration;
	
	public Authorizer(SpecificationBuilder specificationConfiguration) {
		this.specificationConfiguration = specificationConfiguration;
	}

	public void applyValidations(List<Operation> operations) {
		specificationConfiguration
			.getOperationsSpecifications()
			.stream()
			.forEach(s -> s.aplyValidations(operations));
		
		showValidationResult(operations);
	}
	
	private void showValidationResult(List<Operation> operations) {
		operations.stream().forEach(Operation::showValidationResult);
	}

}
