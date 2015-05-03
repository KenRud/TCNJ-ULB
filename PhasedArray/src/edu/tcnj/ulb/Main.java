package edu.tcnj.ulb;

import java.io.IOException;

import edu.tcnj.ulb.daq.DataParser;
import edu.tcnj.ulb.daq.Recording;
import edu.tcnj.ulb.dsp.DataProcessor;
import edu.tcnj.ulb.ui.Menu;
import edu.tcnj.ulb.ui.Prompt;


public class Main {
	public static final int BYTES_PER_SECOND = 360000;
	public static final int BYTES_PER_MINUTE = BYTES_PER_SECOND * 60;
	public static final int CHUNK_SIZE = 1000;
	public static final int NUM_CHANNELS = 9;
	
	public static void main(String[] args) throws Exception {
		Menu mainMenu = new Menu("TCNJ Underwater Beacon Locator");
		mainMenu.addSelection("Record live data from hydrophone array", Main::recordLiveData);
		mainMenu.addSelection("Process data from a file", Main::processFile);
		mainMenu.addSelection("Record and process live data", Main::recordAndProcess);
		mainMenu.display();
	}
	
	private static void recordLiveData() {
		String path = Prompt.getString("Recording destination");
		Recording recording = Recording.create(path, Recording.BYTES_PER_SECOND * 10);
		recording.start();
		
		try {
			Thread.sleep(60000); // Record for a minute
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			recording.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Program terminated.");
	}
	
	private static void processFile() {
		String path = Prompt.getString("Recording source path");
		try {
			Recording recording = Recording.load(path);
			System.out.printf("Timestamp: %s%n", recording.getMetaData().getTimestamp());
			System.out.printf("Filenames: %s%n", recording.getMetaData().getFilenames());
			System.out.printf("File size: %d%n", recording.getMetaData().fileSize());
			System.out.printf("Stop position: %d%n", recording.getMetaData().getStopPosition());
			DataParser parser = new DataParser(recording, NUM_CHANNELS, CHUNK_SIZE);
			DataProcessor processor = new DataProcessor(parser);
			processor.process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void recordAndProcess() {
//		String filename = Prompt.getString("Enter a destination directory name");
//		
//		try (ArduinoReader reader = new ArduinoReader(filename, FILE_SIZE)) {
//			reader.start();
//			DataParser parser = new DataParser(reader, NUM_CHANNELS, CHUNK_SIZE);
//			DataProcessor processor = new DataProcessor(parser);
//			processor.process();
//		}
//		
//		System.out.println("Program terminated.");
	}
}
