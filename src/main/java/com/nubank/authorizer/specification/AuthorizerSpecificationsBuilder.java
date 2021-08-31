package com.nubank.authorizer.specification;

import java.util.HashSet;
import java.util.Set;

import com.nubank.authorizer.specification.impl.AllowOnlyOneAccountSpecification;
import com.nubank.authorizer.specification.impl.AllowTransactionOnlyAfterAccountInitializationSpecification;
import com.nubank.authorizer.specification.impl.AllowTransactionOnlyWhenAccountHasActiveCardSpecification;
import com.nubank.authorizer.specification.impl.AllowTransactionOnlyWithInsEnoughLimit;
import com.nubank.authorizer.specification.impl.DoubledTransactionSpecification;
import com.nubank.authorizer.specification.impl.HighFrequencySmalIntervalSpecification;

public class AuthorizerSpecificationsBuilder implements SpecificationsBuilder{

	private Set<Specification> specifications;
	
	public AuthorizerSpecificationsBuilder() {
		specifications = new HashSet<>();
	}
	
	@Override
	public Set<Specification> getOperationsSpecifications() {
		specifications.add(new AllowOnlyOneAccountSpecification());
		specifications.add(new AllowTransactionOnlyAfterAccountInitializationSpecification());
		specifications.add(new AllowTransactionOnlyWhenAccountHasActiveCardSpecification());
		specifications.add(new AllowTransactionOnlyWithInsEnoughLimit());
		specifications.add(new HighFrequencySmalIntervalSpecification());
		specifications.add(new DoubledTransactionSpecification());
		
		return specifications;
	}

}
