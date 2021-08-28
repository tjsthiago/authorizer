package com.nubank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.nubank.model.CreateAccount;
import com.nubank.model.Operation;
import com.nubank.model.Transaction;
import com.nubank.parser.DateUtils;
import com.nubank.parser.OperationParser;

public class ApplicationTest {
	
	private OperationParser operationParser;
	
	@Before
	public void setup() {
		operationParser = new OperationParser();
	}

	@Test
	public void convertOperationsToJson() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": true, \"available-limit\": 100}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Habbib's\", \"amount\": 90, \"time\": \"2019-02-13T11:00:00.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"McDonald's\", \"amount\": 30, \"time\": \"2019-02-13T12:00:00.000Z\"}}"
		);

		List<Operation> operations = new ArrayList<>();
		
		operationsInput.stream().forEach(operation -> operations.add(operationParser.parse(operation)));
		
		assertNotNull(operations);
		
		assertEquals("com.nubank.model.CreateAccount", operations.get(0).getClass().getName());
		CreateAccount createAccount = (CreateAccount) operations.get(0);
		assertTrue(createAccount.isActive_card());
		assertEquals(100, createAccount.getAvailable_limit());
		
		assertEquals("com.nubank.model.Transaction", operations.get(1).getClass().getName());
		Transaction firstTransaction = (Transaction) operations.get(1);
		assertEquals("Burger King", firstTransaction.getMerchant());
		assertEquals(20, firstTransaction.getAmount());
		assertEquals(DateUtils.convertStringToDate("2019-02-13T10:00:00.000Z"), firstTransaction.getTime());
		
		assertEquals("com.nubank.model.Transaction", operations.get(2).getClass().getName());
		Transaction secondTransaction = (Transaction) operations.get(2);
		assertEquals("Habbib's", secondTransaction.getMerchant());
		assertEquals(90, secondTransaction.getAmount());
		assertEquals(DateUtils.convertStringToDate("2019-02-13T11:00:00.000Z"), secondTransaction.getTime());
		
		assertEquals("com.nubank.model.Transaction", operations.get(3).getClass().getName());
		Transaction thirdTransaction = (Transaction) operations.get(3);
		assertEquals("McDonald's", thirdTransaction.getMerchant());
		assertEquals(30, thirdTransaction.getAmount());
		assertEquals(DateUtils.convertStringToDate("2019-02-13T12:00:00.000Z"), thirdTransaction.getTime());
	}
	
}
