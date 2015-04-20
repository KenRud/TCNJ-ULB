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
		int x = 1;
		for(int i = 0; i < parser.numChannels(); i++){
			short[] channelData = parser.getChannel(i).get(index, length);
			chunkWindow[i] = channelData;

			// Testing FFT on random window
			if(x == 1){
				computeFFT(channelData);
				x++;
			}

			for(int j = 0; j < channelData.length; j++){
				//System.out.println("Channel " + i + ": " + channelData[j]);
			} 
		}

		// TODO: send this window off for processing


	}

	private void computeFFT(short[] timeDelayedSignal){

		Complex[] complexSignal = new Complex[WINDOW_SIZE];
		Complex temp;

		// Convert the time delayed output into complex numbers for use in the FFT.java program
		for(int i = 0; i < WINDOW_SIZE; i++){
			temp = new Complex(timeDelayedSignal[i], 0);
			complexSignal[i] = temp;
			System.out.println(complexSignal[i].toString());
		}

		Complex[] frequencyResponse = FFT.fft(complexSignal);
		double[] magnitude = computeMagnitude(frequencyResponse);
		//FFT.show(frequencyResponse, "frequencyResponse = fft(complexSignal)");
		FFT.show(magnitude, "magnitude = frequencyResponse.forEach() --> abs()");
	}

	private double[] computeMagnitude(Complex[] x){

		double[] magnitudeValues = new double[x.length];

		for(int i = 0; i < x.length; i++){
			magnitudeValues[i] = x[i].abs();
		}
		return magnitudeValues;
	}

}
