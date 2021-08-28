package com.nubank.operations.create.account;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nubank.operations.Operation;

public class CreateAccount implements Operation{
	private boolean active_card;
	private int available_limit;
	private List<String> violations;

	public CreateAccount(JsonObject operationAsJson) {
		JsonElement accountElement = operationAsJson.get("account");
		this.active_card = accountElement.getAsJsonObject().get("active-card").getAsBoolean();
		this.available_limit = accountElement.getAsJsonObject().get("available-limit").getAsInt();
		this.violations = new ArrayList<>();
	}
	
	public CreateAccount(boolean active_card, int available_limit, List<String> violations) {
		this.active_card = active_card;
		this.available_limit = available_limit;
		this.violations = violations;
	}

	public boolean isActive_card() {
		return active_card;
	}

	public int getAvailable_limit() {
		return available_limit;
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
			createAccount.isActive_card(), 
			createAccount.getAvailable_limit(), 
			currentViolations
		);
	}

	public void showValidationResult() {
		System.out.println(new Gson().toJson(this));
	}
}
