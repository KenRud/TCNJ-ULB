package edu.tcnj.ulb.dsp;

import java.util.Arrays;

import edu.tcnj.ulb.daq.DataParser;

public class DataProcessor {
	private static final int WINDOW_SIZE = 512;
	private static final int TRANSMITTER_FREQUENCY = 6300;
	private static final double THETA_INCREMENT = 1;
	private static final double PHI_INCREMENT = 1;
	
	public static final int SAMPLE_FREQUENCY = 20000;

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
			processAllAngles(window);
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
	
	private void processAllAngles(short[][] window) {
		for (double theta = 0; theta < 360; theta += THETA_INCREMENT) {
			for (double phi = 0; phi < 90; phi += PHI_INCREMENT) {
				short[] combinedWindow = combine(window, phi, theta);
				computeFFT(combinedWindow);
			}
		}
	}
	
	private short[] combine(short[][] window, double phi, double theta) {
		PhasedArray array = new PhasedArray(phi, theta);
		
		// Compute the delayed window
		short[][] delayedWindow = new short[window.length][];
		for (int channel = 0; channel < window.length; channel++) {
			int delay = array.getDelay(channel);
			short[] samples = delayedWindow[channel];
			short[] delayedSamples = new short[samples.length + delay];
			System.arraycopy(samples, 0, delayedSamples, delay, samples.length);
			delayedWindow[channel] = delayedSamples;
		}
		
		int idx;
		short[] combinedWindow = new short[];
		while (true) {
			
		}
		
//		int length = Arrays.stream(delayedWindow).map(w -> w.length).max((a, b) -> a - b).get();
		
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
		System.out.println("Resolution: " + FFT.calculateResolution(magnitude, SAMPLE_FREQUENCY));
		int[] points = desiredElements(FFT.calculateResolution(magnitude, SAMPLE_FREQUENCY));
		for(int i = 0; i < points.length; i++){
			System.out.println("I" + i + "  " + points[i]);
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
}
