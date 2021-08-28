package com.nubank.model;

import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nubank.parser.DateUtils;

public class Transaction implements Operation{
	private String merchant;
	private int amount;
	private Date time;
	

	public Transaction(JsonObject operationAsJson) {
		JsonElement transactonElement = operationAsJson.get("transaction");
		this.merchant = transactonElement.getAsJsonObject().get("merchant").getAsString();
		this.amount = transactonElement.getAsJsonObject().get("amount").getAsInt();
		this.time = DateUtils.convertStringToDate(transactonElement.getAsJsonObject().get("time").getAsString());
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

}
