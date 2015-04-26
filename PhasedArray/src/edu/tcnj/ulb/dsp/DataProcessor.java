package edu.tcnj.ulb.dsp;

import edu.tcnj.ulb.daq.DataParser;

public class DataProcessor {
	public static final int WINDOW_SIZE = 512;
	public static final int TRANSMITTER_FREQUENCY = 6300;
	
	private final DataParser parser;

	public DataProcessor(DataParser parser) {
		this.parser = parser;
	}

	public void process() {
		// TODO Check if this is the correct logic for looping
		// Currently it is set to loop through all windows, sliding the window over the entire 
		// length of a single window.
		for(int idx = 0; idx < parser.channelSize(); idx += WINDOW_SIZE) {
			short[][] window = assembleWindow(idx, WINDOW_SIZE);
			short[] combinedWindow = combine(window);
			computeFFT(combinedWindow);
		}
	}

	private short[][] assembleWindow(int index, int length){
		short[][] chunkWindow = new short[parser.numChannels()][];
		int x = 1; // TODO Is this "x" being used for anything?
		for(int i = 0; i < parser.numChannels(); i++){
			short[] channelData = parser.getChannel(i).get(index, length);
			chunkWindow[i] = channelData;
		}
		return chunkWindow;
	}
	
	private short[] combine(short[][] window) {
		return window[1];
	}

	private void computeFFT(short[] timeDelayedSignal){
		Complex[] complexSignal = new Complex[WINDOW_SIZE];
		Complex temp;

		// Convert the time delayed output into complex numbers for use in the FFT.java program
		for(int i = 0; i < WINDOW_SIZE; i++){
			temp = new Complex(timeDelayedSignal[i], 0);
			complexSignal[i] = temp;
		}

		Complex[] frequencyResponse = FFT.fft(complexSignal);
		double[] magnitude = computeMagnitude(frequencyResponse);
		System.out.println(magnitude.length);
		System.out.println("Resolution: " + FFT.calculateResolution(magnitude, 20000));
		int[] points = desiredElements(FFT.calculateResolution(magnitude, 20000));
		for(int i = 0; i < points.length; i++){
			//System.out.println("I" + i + "  " + points[i]);
		}

		boolean isMatch = matchDetection(points, magnitude);
		if(isMatch){
			System.out.println("Is Match " + isMatch);
		}
		//FFT.show(frequencyResponse, "frequencyResponse = fft(complexSignal)");
		//FFT.show(magnitude, "magnitude = frequencyResponse.forEach() --> abs()");
	}

	private double[] computeMagnitude(Complex[] x){
		double[] magnitudeValues = new double[x.length];

		for(int i = 0; i < x.length; i++){
			magnitudeValues[i] = x[i].abs();
		}
		return magnitudeValues;
	}

	private int[] desiredElements(double resolution){
		int[] indices = new int[5];
		indices[2] = TRANSMITTER_FREQUENCY / (int) resolution;

		for(int i = 0; i < 5; i++){
			if(i !=2){
				if(i < 2){
					indices[i] = indices[2] + (i - 2); 
				} else{
					indices[i] = indices[2] + (i - 2);
				}
			}
		}
		
		return indices;
	}

	private boolean matchDetection(int[] indices, double[] magnitude){

		boolean matchFound= false;
		double averagePower = 0;
		for (int i = 0; i < magnitude.length; i++){
			averagePower = averagePower + magnitude[i];
		}

		averagePower = averagePower / magnitude.length;

		for (int i = 0; i < indices.length; i++){
			// If our 6.3kHz points are greater than 1.5 times the energy, match
			// This should be worked out correctly
			if (magnitude[indices[i]] > (1.5 * averagePower)) {
				matchFound = true;
			}
		}

		return matchFound;

	}
}
