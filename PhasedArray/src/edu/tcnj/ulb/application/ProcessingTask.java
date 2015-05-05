package edu.tcnj.ulb.application;

import javafx.concurrent.Task;
import edu.tcnj.ulb.daq.Recording;
import edu.tcnj.ulb.dsp.DataProcessor;

public class ProcessingTask extends Task<Void> {
	
	private final Recording recording;
	private final DataProcessor processor;

	public ProcessingTask(Recording recording) {
		this.recording = recording;
		this.processor = new DataProcessor(recording);
	}
	
	public ProcessingResults getResults() {
		return processor.getResults();
	}
	
	@Override
	protected Void call() throws Exception {
		while (!isCancelled() && processor.hasNextWindow()) {
			processor.processNextWindow();
			processor.getResults().render();
		}
		return null;
	}

}
