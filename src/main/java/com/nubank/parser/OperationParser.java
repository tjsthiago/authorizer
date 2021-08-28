package com.nubank.parser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nubank.operations.Operation;
import com.nubank.operations.create.account.CreateAccount;
import com.nubank.operations.transaction.Transaction;

public class OperationParser {
	
	public Operation parse(String operation) {
		JsonObject operationAsJson = convertOperationAsStringToJson(operation);
		
		if(isCreateAccountOperation(operationAsJson)) {
			return new CreateAccount(operationAsJson);
		}
		
		return new Transaction(operationAsJson);
	}

	private JsonObject convertOperationAsStringToJson(String operation) {
		return new Gson().fromJson(operation, JsonObject.class);
	}
	
	private boolean isCreateAccountOperation(JsonObject operationAsJson) {
		return operationAsJson.get("account") != null;
	}
}
