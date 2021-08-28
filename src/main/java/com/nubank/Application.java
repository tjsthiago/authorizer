package com.nubank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.nubank.authorizer.Authorizer;
import com.nubank.authorizer.specification.AuthorizerSpecifications;
import com.nubank.authorizer.specification.SpecificationBuilder;
import com.nubank.operations.Operation;
import com.nubank.parser.OperationParser;

public class Application {
	private OperationParser operationParser;
	private SpecificationBuilder specification;
	private Authorizer authorizer;
	
	public Application() {
		operationParser = new OperationParser();
		specification = new AuthorizerSpecifications();
		authorizer = new Authorizer(specification);
	}
	
	public void applyValidations(List<String> operationsInput) {
		List<Operation> operations = parseOperations(operationsInput);
		authorizer.applyValidations(operations);
	}

	public static void main(String[] args) throws IOException {
		Application application = new Application();

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
			List<String> operationsFromStdIn = application.loadOperationsFromStdIn(bufferedReader);
			application.applyValidations(operationsFromStdIn);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private List<String> loadOperationsFromStdIn(BufferedReader bufferedReader) throws IOException {
		List<String> operationsInput = new ArrayList<>();
		String operation;
		
		while ((operation = bufferedReader.readLine()) != null) {
			operationsInput.add(operation);
		}

		return operationsInput;
	}

	private  List<Operation> parseOperations(List<String> operationsInput) {
		List<Operation> operations = new ArrayList<>(operationsInput.size());

		operationsInput.stream().forEach(operation -> operations.add(operationParser.parse(operation)));

		return operations;
	}

}
