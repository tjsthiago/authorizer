package com.nubank.operations.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nubank.operations.Operation;
import com.nubank.parser.DateUtils;

public class Transaction implements Operation{
	private String merchant;
	private int amount;
	private Date time;
	private List<String> violations;

	public Transaction(JsonObject operationAsJson) {
		JsonElement transactonElement = operationAsJson.get("transaction");
		this.merchant = transactonElement.getAsJsonObject().get("merchant").getAsString();
		this.amount = transactonElement.getAsJsonObject().get("amount").getAsInt();
		this.time = DateUtils.convertStringToDate(transactonElement.getAsJsonObject().get("time").getAsString());
		this.violations = new ArrayList<>();
	}

	public String getMerchant() {
		return merchant;
	}

	public int getAmount() {
		return amount;
	}

	public Date getTime() {
		return time;
	}

	public List<String> getViolations() {
		return violations;
	}

	@Override
	public Operation addViolation(Operation operation, String violation) {
		return null;
	}
	
	public void showValidationResult() {
		System.out.println(new Gson().toJson(this));
	}

}
