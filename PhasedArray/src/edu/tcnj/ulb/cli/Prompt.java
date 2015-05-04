package edu.tcnj.ulb.cli;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Prompt {
	private static final Scanner scanner = new Scanner(System.in);
	
	public static void pause() {
		System.out.println("Press ENTER to continue");
		scanner.nextLine();
	}
	
	public static String getString(String description) {
		System.out.format("%s: ", description);
		return scanner.nextLine();
	}
	
	public static String getString(String description, String regex) {
		String answer = Prompt.getString(description);
		if (Pattern.matches(regex, answer)) {
			return answer;
		}
		return Prompt.getString("Invalid input, try again", regex);
	}
	
	public static <T> T getChoice(T[] choices) {
		for (int i = 0; i < choices.length; i++) {
			System.out.printf("%02d) %s\n", i+1, choices[i]);
		}
		System.out.println();
		return choices[Prompt.getInteger(1, choices.length) - 1];
	}
	
	public static <T> T getChoice(List<T> choices) {
		for (int i = 0; i < choices.size(); i++) {
			System.out.printf("%02d) %s\n", i+1, choices.get(i));
		}
		System.out.println();
		return choices.get(Prompt.getInteger(1, choices.size()) - 1);
	}
	
	public static int getInteger(int low, int high) {
		if (low > high) {
			throw new RuntimeException("Lower bound must not be greater than the upper bound.");
		}
		
		int choice;
		String message = String.format("Enter a valid integer [%d-%d]", low, high);
		
		do {
			try {
				choice = Integer.parseInt(Prompt.getString(message));
			} catch (NumberFormatException nfe) {
				choice = -1;
			}
		} while (choice < low || high < choice);
		return choice;
	}
}
