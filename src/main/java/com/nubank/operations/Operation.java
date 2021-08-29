package com.nubank.operations;

import java.util.List;

public interface Operation {

	public List<String> getViolations();
	
	public Operation addViolation(Operation operation, String violation);
	
}
