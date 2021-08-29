package com.nubank.operations.create.account;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nubank.operations.Operation;

public class CreateAccount implements Operation{
	private boolean activeCard;
	private int availableLimit;
	private List<String> violations;

	public CreateAccount(JsonObject operationAsJson) {
		JsonElement accountElement = operationAsJson.get("account");
		this.activeCard = accountElement.getAsJsonObject().get("active-card").getAsBoolean();
		this.availableLimit = accountElement.getAsJsonObject().get("available-limit").getAsInt();
		this.violations = new ArrayList<>();
	}
	
	public CreateAccount(boolean activeCard, int availableLimit, List<String> violations) {
		this.activeCard = activeCard;
		this.availableLimit = availableLimit;
		this.violations = violations;
	}

	public boolean isActiveCard() {
		return activeCard;
	}

	public int getAvailableLimit() {
		return availableLimit;
	}

	public List<String> getViolations() {
		return violations;
	}

	@Override
	public Operation addViolation(Operation operation, String violation) {
		CreateAccount createAccount = (CreateAccount) operation;
		List<String> currentViolations = createAccount.getViolations();
		currentViolations.add(violation);
		
		return new CreateAccount(
			createAccount.isActiveCard(), 
			createAccount.getAvailableLimit(),
			currentViolations
		);
	}

}
