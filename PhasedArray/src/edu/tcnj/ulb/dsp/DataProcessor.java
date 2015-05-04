package edu.tcnj.ulb.dsp;

import edu.tcnj.ulb.Configuration;
import edu.tcnj.ulb.application.MainController;
import edu.tcnj.ulb.daq.DataParser;

public class DataProcessor {
	private static final int WINDOW_SIZE = 512;
	private static final double THETA_INCREMENT = 45;
	private static final double PHI_INCREMENT = 45;
	
	public static double[] SEARCH_SIGNAL = new double[WINDOW_SIZE];

	private final DataParser parser;
	private int windowIndex;

	static {
		// Amplitude needs to be determined
		double amplitude = 1.0;
		for(int sample = 0; sample < WINDOW_SIZE; sample++){
			double time = (double) sample / Configuration.SAMPLE_FREQUENCY;
			// TODO: TEST THIS!
			SEARCH_SIGNAL[sample] = amplitude
					* Math.sin(2 * Math.PI * Configuration.TRANSMITTER_FREQUENCY * time);
		}
	}

	public DataProcessor(DataParser parser) {
		this.parser = parser;
	}
	
	public DataProcessor(DataParser parser, MainController controller){
		this.parser = parser;
	}
	
	public boolean hasNextWindow() {
		return windowIndex + WINDOW_SIZE < parser.channelSize();
	}
	
	public void processNextWindow() {
		short[][] window = assembleWindow(windowIndex, WINDOW_SIZE);
		processAllAngles(window);
		windowIndex += WINDOW_SIZE;
	}

	public void processAll() {
		while(hasNextWindow()) {
			processNextWindow();
		}
	}

	private short[][] assembleWindow(int index, int length){
		short[][] chunkWindow = new short[parser.numChannels()][];
		for(int i = 0; i < parser.numChannels(); i++){
			short[] channelData = parser.getChannel(i).get(index, length);
			chunkWindow[i] = channelData;
		}
		return chunkWindow;
	}
	
	private void processAllAngles(short[][] window) {
		for (double theta = 0; theta < 360; theta += THETA_INCREMENT) {
			for (double phi = 0; phi < 45; phi += PHI_INCREMENT) {
				PhasedArray array = new PhasedArray(phi, theta, window);
				int[] combinedSignal = array.combineChannels();
				computeFFT(combinedSignal);
				computeXCorr(combinedSignal);
			}
		}
	}
	
	private void computeXCorr(int[] timeDelayedSignal){
		double[] signal = copyFromIntArray(timeDelayedSignal);
		double[] crossCorrelation = Correlation.xcorr(signal, SEARCH_SIGNAL);

		for (int i = 0; i < crossCorrelation.length; i++) {
			System.out.println(i + " : " + crossCorrelation[i]);
		}
		System.out.println("signal Length :" + signal.length);
	}
	
	private void computeFFT(int[] timeDelayedSignal){
		Complex[] complexSignal = new Complex[WINDOW_SIZE];
		Complex temp;

		// Convert the time delayed output into complex numbers for use in the FFT.java program
		for(int i = 0; i < WINDOW_SIZE; i++){
			temp = new Complex(timeDelayedSignal[i], 0);
			complexSignal[i] = temp;
		}

		Complex[] frequencyResponse = FFT.fft(complexSignal);
		double[] magnitude = computeMagnitude(frequencyResponse);
		//controller.updateFFTGraph(magnitude);

		int[] points = desiredElements(FFT.calculateResolution(magnitude, 20000));

		boolean isMatch = matchDetectionFFT(points, magnitude);
		if(isMatch){
			//System.out.println("Is Match " + isMatch);
		}
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
		indices[2] = Configuration.TRANSMITTER_FREQUENCY / (int) resolution;

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
	
	private boolean matchDetectionFFT(int[] indices, double[] magnitude){
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

	public static double[] copyFromIntArray(int[] source) {
		double[] dest = new double[source.length];
		for(int i=0; i<source.length; i++) {
			dest[i] = source[i];
		}
		return dest;
	}

}
