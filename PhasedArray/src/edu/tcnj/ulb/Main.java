package edu.tcnj.ulb;

import java.io.IOException;

import edu.tcnj.ulb.ui.Menu;
import edu.tcnj.ulb.ui.Prompt;


public class Main {
	private static final int FILE_SIZE = ArduinoReader.BYTES_PER_SECOND * 10; // ~10 secs of data

	public static void main(String[] args) throws Exception {
		Menu mainMenu = new Menu("TCNJ Underwater Beacon Locator");
		mainMenu.addSelection("Record live data from hydrophone array", Main::recordLiveData);
		mainMenu.addSelection("Process data from a file", null);
		mainMenu.addSelection("Record and process live data", Main::recordAndProcess);
		mainMenu.display();
	}
	
	private static void recordLiveData() {
		String filename = Prompt.getString("Enter a destination file name");
		
		try (ArduinoReader reader = new ArduinoReader(filename, FILE_SIZE)) {
			System.out.println("Recording started.");
			reader.start();
			int i = 0;
			while(reader.hasRemaining()) {
				Thread.sleep(5000);
				System.out.printf("%d seconds have recorded\n", ++i * 5);
			}
			System.out.println("Recording complete, transferring to file.");
		} catch (IOException e) {
			System.out.printf("Unable to open the file: %s\n", e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Program terminated.");
	}
	
	private static void recordAndProcess() {
		String filename = Prompt.getString("Enter a destination file name");
		
		try (ArduinoReader reader = new ArduinoReader(filename, FILE_SIZE)) {
			System.out.println("Recording started.");
			reader.start();
			DataParser parser = new DataParser(reader, 9, 1000);
			for (Short[] chunk : parser.getChannel(0)) {
				System.out.print(chunk[0]);
			}
			System.out.println("\nRecording complete, transferring to file...");
		} catch (IOException e) {
			System.out.printf("Unable to open the file: %s\n", e.getMessage());
		}
		
		System.out.println("Program terminated.");
	}
}