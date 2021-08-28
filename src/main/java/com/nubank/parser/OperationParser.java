package com.nubank.parser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nubank.model.CreateAccount;
import com.nubank.model.Operation;
import com.nubank.model.Transaction;

public class OperationParser {
	
	private Gson gson;
	
	public OperationParser() {
		this.gson = new Gson();
	}
	
	public Operation parse(String operation) {
		JsonObject operationAsJson = convertOperationAsStringToJson(operation);
		
		if(isCreateAccountOperation(operationAsJson)) {
			return new CreateAccount(operationAsJson);
		}
		
		return new Transaction(operationAsJson);
	}

	private JsonObject convertOperationAsStringToJson(String operation) {
		return gson.fromJson(operation, JsonObject.class);
	}
	
	private boolean isCreateAccountOperation(JsonObject operationAsJson) {
		return operationAsJson.get("account") != null;
	}
}
