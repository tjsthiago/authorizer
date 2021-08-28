package com.nubank.authorizer.specification;

import java.util.HashSet;
import java.util.Set;

import com.nubank.authorizer.specification.account.AllowOnlyOneAccountSpecification;

public class AuthorizerSpecifications implements SpecificationBuilder{

	private Set<Specification> specifications;
	
	public AuthorizerSpecifications() {
		specifications = new HashSet<>();
	}
	
	@Override
	public Set<Specification> getOperationsSpecifications() {
		specifications.add(new AllowOnlyOneAccountSpecification());
		
		return specifications;
	}

}
