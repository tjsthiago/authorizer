package com.nubank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.nubank.authorizer.Authorizer;
import com.nubank.authorizer.specification.AuthorizerSpecifications;
import com.nubank.operations.Operation;
import com.nubank.operations.create.account.CreateAccount;
import com.nubank.operations.transaction.Transaction;
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
		
		List<Operation> operations = loadOperations(operationsInput);
		
		assertNotNull(operations);
		
		assertEquals("com.nubank.operations.create.account.CreateAccount", operations.get(0).getClass().getName());
		CreateAccount createAccount = (CreateAccount) operations.get(0);
		assertTrue(createAccount.isActive_card());
		assertEquals(100, createAccount.getAvailable_limit());
		
		assertEquals("com.nubank.operations.transaction.Transaction", operations.get(1).getClass().getName());
		Transaction firstTransaction = (Transaction) operations.get(1);
		assertEquals("Burger King", firstTransaction.getMerchant());
		assertEquals(20, firstTransaction.getAmount());
		assertEquals(DateUtils.convertStringToDate("2019-02-13T10:00:00.000Z"), firstTransaction.getTime());
		
		assertEquals("com.nubank.operations.transaction.Transaction", operations.get(2).getClass().getName());
		Transaction secondTransaction = (Transaction) operations.get(2);
		assertEquals("Habbib's", secondTransaction.getMerchant());
		assertEquals(90, secondTransaction.getAmount());
		assertEquals(DateUtils.convertStringToDate("2019-02-13T11:00:00.000Z"), secondTransaction.getTime());
		
		assertEquals("com.nubank.operations.transaction.Transaction", operations.get(3).getClass().getName());
		Transaction thirdTransaction = (Transaction) operations.get(3);
		assertEquals("McDonald's", thirdTransaction.getMerchant());
		assertEquals(30, thirdTransaction.getAmount());
		assertEquals(DateUtils.convertStringToDate("2019-02-13T12:00:00.000Z"), thirdTransaction.getTime());
	}
	
	@Test
	public void assertThatOneAccountIsCreatedWithSuccess() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": false, \"available-limit\": 750}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		assertNotNull(operations);
		assertTrue(operations.stream().noneMatch(o -> o.getViolations().size() > 0));
	}
	
	@Test
	public void assertThatOnlyOneAccountIsCreatedWithSuccess() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": true, \"available-limit\": 175}}",
			"{\"account\": {\"active-card\": true, \"available-limit\": 350}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecifications()
		);
		
		authorizer.applyValidations(operations);
		
		Operation operation = getFirstOperationWithViolations(operations);
		Optional<String> violation = getViolationByRestriction(operation, "accountalready-initialized");
		
		assertTrue(violation.isPresent());
		assertFalse(operations.stream().noneMatch(o -> o.getViolations().size() > 0));
	}

	@Test
	public void assertThatTransactionWithoutAccountInitializationHasViolation() {
		List<String> operationsInput = Arrays.asList(
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecifications()
		);
		
		authorizer.applyValidations(operations);
		
		Operation operation = getFirstOperationWithViolations(operations);
		Optional<String> violation = getViolationByRestriction(operation, "account-not-initialized");
		
		assertTrue(violation.isPresent());
		assertFalse(operations.stream().noneMatch((o -> o.getViolations().size() > 0)));
	}

	private List<Operation> loadOperations(List<String> operationsInput) {
		List<Operation> operations = new ArrayList<>(operationsInput.size());
		operationsInput.stream().forEach(operation -> operations.add(operationParser.parse(operation)));
		
		return operations;
	}
	
	private Optional<String> getViolationByRestriction(Operation operation, String restriction) {
		return operation.getViolations().stream().filter(v -> v.equalsIgnoreCase(restriction)).findFirst();
	}

	private Operation getFirstOperationWithViolations(List<Operation> operations) {
		return operations.stream().filter(o -> o.getViolations().size() > 0).findFirst().get();
	}
	
}
