package edu.tcnj.ulb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import edu.tcnj.ulb.daq.ArduinoReader;
import edu.tcnj.ulb.daq.DataParser;
import edu.tcnj.ulb.dsp.DataProcessor;
import edu.tcnj.ulb.ui.Menu;
import edu.tcnj.ulb.ui.Prompt;


public class Main {
	public static final int BYTES_PER_SECOND = 360000;
	public static final int BYTES_PER_MINUTE = BYTES_PER_SECOND * 60;
	public static final int CHUNK_SIZE = 1000;
	public static final int NUM_CHANNELS = 9;
	
	private static final int FILE_SIZE = BYTES_PER_SECOND * 10; // ~10 secs of data

	public static void main(String[] args) throws Exception {
		Menu mainMenu = new Menu("TCNJ Underwater Beacon Locator");
		mainMenu.addSelection("Record live data from hydrophone array", Main::recordLiveData);
		mainMenu.addSelection("Process data from a file", Main::processFile);
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
				// TODO Change this to something else
				Thread.sleep(5000);
				System.out.printf("%d seconds have recorded\n", ++i * 5);
			}
			System.out.println("Recording complete, transferring to file.");
		} catch (FileNotFoundException e) {
			System.out.printf("The specified file could not be found: %s\n", e.getMessage());
		} catch (IOException e) {
			System.out.printf("Unable to open the file: %s\n", e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Program terminated.");
	}
	
	private static void processFile() {
		String filename = Prompt.getString("Enter a source file name");
		
		try (RandomAccessFile file = new RandomAccessFile(filename, "r")) {
			MappedByteBuffer fileBuffer = file.getChannel().map(MapMode.READ_ONLY, 0, file.length());
			DataParser parser = new DataParser(fileBuffer, NUM_CHANNELS, CHUNK_SIZE);
			DataProcessor processor = new DataProcessor(parser);
			processor.process();
		} catch (FileNotFoundException e) {
			System.out.printf("The specified file could not be found: %s\n", e.getMessage());
		} catch (IOException e) {
			System.out.printf("Unable to open the file: %s\n", e.getMessage());
		}
	}
	
	private static void recordAndProcess() {
		String filename = Prompt.getString("Enter a destination file name");
		
		try (ArduinoReader reader = new ArduinoReader(filename, FILE_SIZE)) {
			reader.start();
			DataParser parser = new DataParser(reader, NUM_CHANNELS, CHUNK_SIZE);
			DataProcessor processor = new DataProcessor(parser);
			processor.process();
		} catch (FileNotFoundException e) {
			System.out.printf("The specified file could not be found: %s\n", e.getMessage());
		} catch (IOException e) {
			System.out.printf("Unable to open the file: %s\n", e.getMessage());
		}
		
		System.out.println("Program terminated.");
	}
}
