package edu.tcnj.ulb.dsp;

import edu.tcnj.ulb.daq.DataParser;

public class DataProcessor {
	private final DataParser parser;
	public static final int WINDOW_SIZE = 512;
	public DataProcessor(DataParser parser) {
		this.parser = parser;
	}
	
	public void process() {
		// TODO This is the entry point for processing the data
		// This will be determined by the file size which is being fed.  Ugly to do it this way
		// Better to find way to determine this based on file size and chunk size
		int idx = 0; 
		while(true){
			assembleWindow(idx, WINDOW_SIZE);
			idx++;
		}

	}

	private void assembleWindow(int index, int length){

		short[][] chunkWindow = new short[parser.numChannels()][];

		for(int i = 0; i < parser.numChannels(); i++){
			short[] channelData = parser.getChannel(i).get(index, length);
			chunkWindow[i] = channelData;
			for(int j = 0; j < channelData.length; j++){
				System.out.println("Channel " + i + ": " + channelData[j]);
			} 
		}

		// TODO: send this window off for processing


	}

}
