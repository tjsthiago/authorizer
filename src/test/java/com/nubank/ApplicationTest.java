package com.nubank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.nubank.account.Account;
import com.nubank.authorizer.Authorizer;
import com.nubank.authorizer.specification.AuthorizerSpecificationsBuilder;
import com.nubank.operations.CreateAccount;
import com.nubank.operations.Operation;
import com.nubank.operations.Transaction;
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
		
		assertEquals("com.nubank.operations.CreateAccount", operations.get(0).getClass().getName());
		CreateAccount createAccount = (CreateAccount) operations.get(0);
		assertTrue(createAccount.isActiveCard());
		assertEquals(100, createAccount.getAvailableLimit());
		
		assertEquals("com.nubank.operations.Transaction", operations.get(1).getClass().getName());
		Transaction firstTransaction = (Transaction) operations.get(1);
		assertEquals("Burger King", firstTransaction.getMerchant());
		assertEquals(20, firstTransaction.getAmount());
		assertEquals(DateUtils.convertStringToDate("2019-02-13T10:00:00.000Z"), firstTransaction.getTime());
		
		assertEquals("com.nubank.operations.Transaction", operations.get(2).getClass().getName());
		Transaction secondTransaction = (Transaction) operations.get(2);
		assertEquals("Habbib's", secondTransaction.getMerchant());
		assertEquals(90, secondTransaction.getAmount());
		assertEquals(DateUtils.convertStringToDate("2019-02-13T11:00:00.000Z"), secondTransaction.getTime());
		
		assertEquals("com.nubank.operations.Transaction", operations.get(3).getClass().getName());
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
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		
		assertNotNull(operations);
		assertTrue(operations.stream().noneMatch(o -> o.getViolations().size() > 0));
		assertEquals(750, account.getAvailableLimit());
		
	}
	
	@Test
	public void assertThatOnlyOneAccountIsCreatedWithSuccess() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": true, \"available-limit\": 175}}",
			"{\"account\": {\"active-card\": true, \"available-limit\": 350}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		
		Operation operation = getFirstOperationWithViolations(operations);
		Optional<String> violation = getViolationByRestriction(operation, "accountalready-initialized");
		
		assertTrue(violation.isPresent());
		assertFalse(operations.stream().noneMatch(o -> o.getViolations().size() > 0));
		assertEquals(175, account.getAvailableLimit());
	}

	@Test
	public void givenAnAccountWithActiveCardAndAtransactionWithAmountLessThanAccountAvailableCreditAllowTransaction() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": true, \"available-limit\": 100}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:00.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		
		assertNotNull(operations);
		assertTrue(operations.stream().noneMatch(o -> o.getViolations().size() > 0));
		assertEquals(80, account.getAvailableLimit());
	}
	
	@Test
	public void assertThatTransactionWithoutAccountInitializationHasViolation() {
		List<String> operationsInput = Arrays.asList(
			"{\"transaction\": {\"merchant\": \"Uber Eats\", \"amount\": 25, \"time\": \"2020-12-01T11:07:00.000Z\"}}",
			"{\"account\": {\"active-card\": true, \"available-limit\": 225}}",
			"{\"transaction\": {\"merchant\": \"Uber Eats\", \"amount\": 25, \"time\": \"2020-12-01T11:07:00.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		
		Optional<String> violation = getViolationByRestriction(operations.get(0), "account-not-initialized");
		
		assertTrue(violation.isPresent());
		assertTrue(operations.get(1).getViolations().isEmpty());
		assertTrue(operations.get(2).getViolations().isEmpty());
		assertEquals(200, account.getAvailableLimit());
	}
	
	@Test
	public void givenAnAccountWithDisableCardAndAtransactionWithAmountLessThanAccountAvailableCreditDoNotAllowTransactions() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": false, \"available-limit\": 100}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:00.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Habbib's\", \"amount\": 15, \"time\": \"2019-02-13T11:15:00.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		Optional<String> transactionOneViolation = getViolationByRestriction(operations.get(1), "card-not-active");
		Optional<String> transactionTwoViolation = getViolationByRestriction(operations.get(2), "card-not-active");
		
		assertNotNull(operations);
		assertTrue(transactionOneViolation.isPresent());
		assertTrue(transactionTwoViolation.isPresent());
		
	}
	
	@Test
	public void givenAnAccountWithInsufficientLimitDoNotAllowTransactions() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": true, \"available-limit\": 1000}}",
			"{\"transaction\": {\"merchant\": \"Vivara\", \"amount\": 1250, \"time\": \"2019-02-13T11:00:00.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Samsung\", \"amount\": 2500, \"time\": \"2019-02-13T11:00:01.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Nike\", \"amount\": 800, \"time\": \"2019-02-13T11:01:01.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		Optional<String> transactionOneViolation = getViolationByRestriction(operations.get(1), "insufficient-limit");
		Optional<String> transactionTwoViolation = getViolationByRestriction(operations.get(2), "insufficient-limit");
		Optional<String> transactionThreeViolation = getViolationByRestriction(operations.get(3), "insufficient-limit");
		
		assertNotNull(operations);
		assertTrue(transactionOneViolation.isPresent());
		assertTrue(transactionTwoViolation.isPresent());
		assertFalse(transactionThreeViolation.isPresent());
		assertEquals(200, account.getAvailableLimit());
	}
	
	@Test
	public void assertThatFourthRequestProcessedWithinTwoMinutesOfPreviousOperationContainsHighFrequencySmalIntervalViolation() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": true, \"available-limit\": 100}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:00.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Habbib's\", \"amount\": 20, \"time\": \"2019-02-13T11:00:01.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"McDonald's\", \"amount\": 20, \"time\": \"2019-02-13T11:01:01.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Subway\", \"amount\": 20, \"time\": \"2019-02-13T11:01:31.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T12:00:00.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		Optional<String> operationWithHighFrequencySmalIntervalViolation = getViolationByRestriction(operations.get(4), "high-frequency-small-interval");
		
		assertTrue(operations.get(0).getViolations().isEmpty());
		assertTrue(operations.get(1).getViolations().isEmpty());
		assertTrue(operations.get(2).getViolations().isEmpty());
		assertTrue(operations.get(3).getViolations().isEmpty());
		
		assertFalse(operations.get(4).getViolations().isEmpty());
		assertTrue(operationWithHighFrequencySmalIntervalViolation.isPresent());
		
		assertTrue(operations.get(5).getViolations().isEmpty());
	}
	
	@Test
	public void givemTwoDatesAssertThatDiferenceBetweenThemInSecondsIsLessThanTwoMInutes() {
		Date firstDate = DateUtils.convertStringToDate("2019-02-13T11:00:01.000Z");
		Date secondDate = DateUtils.convertStringToDate("2019-02-13T11:01:01.000Z");

		long secondsInTwoMinutes = 120;
		long diferenceInSecondsBetweenTwoDates = DateUtils.getDiferenceInSecondsBetweenTwoDates(firstDate, secondDate);
		
		assertTrue(diferenceInSecondsBetweenTwoDates < secondsInTwoMinutes);
	}
	
	@Test
	public void givemTwoDatesAssertThatDiferenceBetweenThemInSecondsIsBiggerThanTwoMInutes() {
		Date firstDate = DateUtils.convertStringToDate("2019-02-13T11:00:00.000Z");
		Date secondDate = DateUtils.convertStringToDate("2019-02-13T11:03:00.000Z");

		long secondsInTwoMinutes = 120;
		long diferenceInSecondsBetweenTwoDates = DateUtils.getDiferenceInSecondsBetweenTwoDates(firstDate, secondDate);
		
		assertTrue(diferenceInSecondsBetweenTwoDates > secondsInTwoMinutes);
	}
	
	@Test
	public void assertThatOneOperationWithSameMerchantAndAmountInLessThanTwoMinutesHasDoubledTransactionViolation() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": true, \"available-limit\": 100}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:00.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"McDonald's\", \"amount\": 10, \"time\": \"2019-02-13T11:00:01.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:02.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 15, \"time\": \"2019-02-13T12:00:03.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		Optional<String> operationWithDoubleOperationViolation = getViolationByRestriction(operations.get(3), "doubled-transaction");
		
		assertTrue(operations.get(0).getViolations().isEmpty());
		assertTrue(operations.get(1).getViolations().isEmpty());
		assertTrue(operations.get(2).getViolations().isEmpty());
		
		assertFalse(operations.get(3).getViolations().isEmpty());
		assertTrue(operationWithDoubleOperationViolation.isPresent());
		
		assertTrue(operations.get(4).getViolations().isEmpty());
	}
	
	@Test
	public void getPreviousOperationsFromTransactionTime() {
		List<String> operationsInput = Arrays.asList(
			"{\"transaction\": {\"merchant\": \"001\", \"amount\": 20, \"time\": \"2019-02-13T11:00:00.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"002\", \"amount\": 10, \"time\": \"2019-02-13T11:00:01.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"003\", \"amount\": 20, \"time\": \"2019-02-13T11:00:02.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"004\", \"amount\": 15, \"time\": \"2019-02-13T11:00:03.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"005\", \"amount\": 15, \"time\": \"2019-02-13T11:00:04.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"006\", \"amount\": 15, \"time\": \"2019-02-13T11:00:05.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		List<Transaction> transactions = getTransactionOerations(operations);
		
		Transaction currentTransaction = transactions.get(4);
		
		List<Transaction> beforeTransactions = transactions
				.stream()
				.filter(isBeforeCurrentTransaction(currentTransaction))
				.collect(Collectors.toList());
		
		assertEquals(4, beforeTransactions.size());
	}
	
	@Test
	public void getTwoMInutesPreviousOperationsFromTransactionTime() {
		List<String> operationsInput = Arrays.asList(
			"{\"transaction\": {\"merchant\": \"001\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"002\", \"amount\": 10, \"time\": \"2019-02-13T10:00:01.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"003\", \"amount\": 20, \"time\": \"2019-02-13T11:00:01.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"004\", \"amount\": 15, \"time\": \"2019-02-13T11:00:02.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"005\", \"amount\": 15, \"time\": \"2019-02-13T11:00:03.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"006\", \"amount\": 15, \"time\": \"2019-02-13T11:00:05.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		List<Transaction> transactions = getTransactionOerations(operations);
		
		Transaction currentTransaction = transactions.get(4);
		
		Date currentTransactionTime = currentTransaction.getTime();
		long twoMinutesInSeconds = 120;
		
		List<Transaction> twoMInutesPreviousOperationsFromTransactionTime = transactions
				.stream()
				.filter(isBeforeCurrentTransaction(currentTransaction))
				.filter(t -> getDiferenceInSecondsBetweenTransactions(t.getTime(), currentTransactionTime) < twoMinutesInSeconds)
				.collect(Collectors.toList());
		
		assertEquals("003", twoMInutesPreviousOperationsFromTransactionTime.get(0).getMerchant());
		assertEquals("004", twoMInutesPreviousOperationsFromTransactionTime.get(1).getMerchant());
	}
	
	@Test
	public void assertThatEachOperationThatViolatesMoreThanOneRuleHaveMoreThanOneViolation() {
		List<String> operationsInput = Arrays.asList(
			"{\"account\": {\"active-card\": true, \"available-limit\": 100}}",
			"{\"transaction\": {\"merchant\": \"McDonald's\", \"amount\": 10, \"time\": \"2019-02-13T11:00:01.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:02.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 5, \"time\": \"2019-02-13T11:00:07.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 5, \"time\": \"2019-02-13T11:00:08.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 150, \"time\": \"2019-02-13T11:00:18.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 190, \"time\": \"2019-02-13T11:00:22.000Z\"}}",
			"{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 15, \"time\": \"2019-02-13T12:00:27.000Z\"}}"
		);

		List<Operation> operations = loadOperations(operationsInput);
		
		Account account = new Account();
		
		Authorizer authorizer = new Authorizer(
			new AuthorizerSpecificationsBuilder(),
			account
		);
		
		authorizer.applyValidations(operations);
		
		assertEquals(0, operations.get(0).getViolations().size());
		assertEquals(0, operations.get(1).getViolations().size());
		assertEquals(0, operations.get(2).getViolations().size());
		assertEquals(0, operations.get(3).getViolations().size());
		
		assertEquals(2, operations.get(4).getViolations().size());
		assertEquals(2, operations.get(5).getViolations().size());
		assertEquals(2, operations.get(6).getViolations().size());
		
		assertEquals(0, operations.get(7).getViolations().size());
	}
	
	private long getDiferenceInSecondsBetweenTransactions(Date previousTransactionTime, Date currentTransactionTime) {
		return DateUtils.getDiferenceInSecondsBetweenTwoDates(previousTransactionTime, currentTransactionTime);
	}
	
	private Predicate<? super Transaction> isBeforeCurrentTransaction(Transaction currentTransaction) {
		return t -> currentTransaction.getTime().compareTo(t.getTime()) > 0;
	}
	
	private List<Transaction> getTransactionOerations(List<Operation> operations) {
		return operations.stream().filter(isTransactionOperation()).map(o -> (Transaction) o).collect(Collectors.toList());
	}
	
	private Predicate<? super Operation> isTransactionOperation() {
		return o -> o instanceof Transaction;
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
