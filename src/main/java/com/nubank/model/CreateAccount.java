package com.nubank.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CreateAccount implements Operation{
	private boolean active_card;
	private int available_limit;

	public CreateAccount(JsonObject operationAsJson) {
		JsonElement accountElement = operationAsJson.get("account");
		this.active_card = accountElement.getAsJsonObject().get("active-card").getAsBoolean();
		this.available_limit = accountElement.getAsJsonObject().get("available-limit").getAsInt();
	}

	public boolean isActive_card() {
		return active_card;
	}

	public int getAvailable_limit() {
		return available_limit;
	}
	
}
