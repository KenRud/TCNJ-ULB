package edu.tcnj.ulb.application;

import edu.tcnj.ulb.Configuration;
import edu.tcnj.ulb.daq.DataParser;
import edu.tcnj.ulb.daq.Recording;
import edu.tcnj.ulb.dsp.DataProcessor;
import javafx.concurrent.Task;

public class ProcessingTask extends Task<Void> {
	
	private final Recording recording;

	public ProcessingTask(Recording recording) {
		this.recording = recording;
	}

	@Override
	protected Void call() throws Exception {
		DataParser parser = new DataParser(recording,
				Configuration.NUM_CHANNELS, Configuration.NUM_CHANNELS);
		DataProcessor processor = new DataProcessor(parser);
		while (!isCancelled() && processor.hasNextWindow()) {
			processor.processNextWindow();
		}
		return null;
	}

}
