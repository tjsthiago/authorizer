package com.nubank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Application {
	public static void main(String[] args) throws IOException {
		
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
			List<String> operationsInput = loadOperations(bufferedReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static List<String> loadOperations(BufferedReader bufferedReader) throws IOException {
		List<String> operationsInput = new ArrayList<>();
		String operation;
		while ((operation = bufferedReader.readLine()) != null) {
			operationsInput.add(operation);
		}
		
		return operationsInput;
	}
	
}
