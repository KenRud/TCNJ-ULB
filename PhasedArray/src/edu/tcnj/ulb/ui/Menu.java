package edu.tcnj.ulb.ui;

import java.util.ArrayList;
import java.util.List;

public class Menu {
	private final List<Selection> selections = new ArrayList<>();
	private final String title;
	
	public Menu(String title) {
		this.title = title;
		addSelection("Quit program", () -> {
			System.out.println("Program terminated.");
			System.exit(0);
		});
	}

	public void addSelection(String description, Runnable action) {
		selections.add(new Selection(description, action));
	}
	
	public void display() {
		// Display menu title
		String bar = " ";
		for (int i = 0; i < title.length(); i++) {
			bar += "=";
		}
		System.out.println();
		System.out.println(bar);
		System.out.format("|%s|\n", title);
		System.out.println(bar);

		Selection selection = Prompt.getChoice(selections);
		System.out.println();
		selection.action.run();
	}
	
	public void displayForever() {
		while(true) {
			display();
		}
	}
	
	private static class Selection {
		private final String description;
		private final Runnable action;

		public Selection(String description, Runnable action) {
			this.description = description;
			this.action = action;
		}
		
		@Override
		public String toString() {
			return description;
		}
	}
}
