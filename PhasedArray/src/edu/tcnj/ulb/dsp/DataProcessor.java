package edu.tcnj.ulb.dsp;

import java.time.Instant;

import edu.tcnj.ulb.Configuration;
import edu.tcnj.ulb.application.ProcessingResults;
import edu.tcnj.ulb.daq.DataParser;
import edu.tcnj.ulb.daq.Recording;

public class DataProcessor {
	private static final int WINDOW_SIZE = 1024;
	private static final double THETA_INCREMENT = 45;
	private static final double PHI_INCREMENT = 45;
	private static final double[] EMPTY_SIGNAL = new double[0];
	public static final double[] EMPTY_FFT = new double[0];

	public static double[] SEARCH_SIGNAL = new double[WINDOW_SIZE];

	private final Recording recording;
	private final ProcessingResults results;
	private final DataParser parser;
	private long windowIndex;
	
	// Used for displaying the best matching signal
	private double[] bestMatchSignal;
	private double[] fftMatchSignal;
	private double maxMatchMagnitude;

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

	public DataProcessor(Recording recording) {
		this.recording = recording;
		this.results = new ProcessingResults(Configuration.SAMPLE_FREQUENCY);
		this.parser = new DataParser(recording, Configuration.NUM_CHANNELS,
				Configuration.NUM_CHANNELS);
	}
	
	public boolean hasNextWindow() {
		return windowIndex + WINDOW_SIZE < parser.channelSize();
	}
	
	public void processNextWindow() {
		short[][] window = assembleWindow(windowIndex, WINDOW_SIZE);
		resetBestMatch();
		processAllAngles(window);
		results.setBestMatchSignal(calculateTime(windowIndex), bestMatchSignal);
		results.setFFTSignal(fftMatchSignal);
		windowIndex += WINDOW_SIZE;
	}

	private void resetBestMatch() {
		bestMatchSignal = EMPTY_SIGNAL;
		fftMatchSignal = EMPTY_FFT;
		maxMatchMagnitude = 0;
	}

	private Instant calculateTime(long windowIndex) {
		return recording.getMetaData().getTimestamp()
				.plusSeconds(windowIndex / Configuration.SAMPLE_FREQUENCY);
	}

	public void processAll() {
		while(hasNextWindow()) {
			processNextWindow();
		}
	}
	
	public ProcessingResults getResults() {
		return results;
	}

	private short[][] assembleWindow(long index, int length){
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
				double[] combinedSignal = array.combineChannels();
				double[] fft = computeFFT(combinedSignal);
				computeXCorr(combinedSignal);
				
				// TODO Refactor!
				// Grabbing the signal with the best match
				double max = selectMaxMagnitude(fft);
				if (max > maxMatchMagnitude) {
					maxMatchMagnitude = max;
					bestMatchSignal = combinedSignal;
					fftMatchSignal = fft;
				}
			}
		}
	}
	
	private double selectMaxMagnitude(double[] magnitude) {
		int[] elements = desiredElementsFFT(magnitude);
		double max = 0;
		for (int j = 0; j < elements.length; j++) {
			max = Math.max(max, magnitude[elements[j]]);
		}
		return max;
	}
	
	private void computeXCorr(double[] timeDelayedSignal) {
		double[] crossCorrelation = Correlation.xcorr(timeDelayedSignal, SEARCH_SIGNAL);
		// Check for peak in cross correlation output
		boolean isMatch = matchDetectionXCorr(crossCorrelation);
	}

	private double[] computeFFT(double[] timeDelayedSignal) {
		Complex[] complexSignal = new Complex[WINDOW_SIZE];
		Complex temp;

		// Convert the time delayed output into complex numbers for use in the FFT.java program
		for(int i = 0; i < WINDOW_SIZE; i++){
			temp = new Complex(timeDelayedSignal[i], 0);
			complexSignal[i] = temp;
		}

		Complex[] frequencyResponse = FFT.fft(complexSignal);
		double[] magnitude = computeMagnitude(frequencyResponse);

		int[] points = desiredElementsFFT(magnitude);

		boolean isMatch = matchDetectionFFT(points, magnitude);
		if(isMatch){
			//System.out.println("Is Match " + isMatch);
		}

		return magnitude;
	}
	
	private double[] computeMagnitude(Complex[] x){
		double[] magnitudeValues = new double[x.length];

		for(int i = 0; i < x.length; i++){
			magnitudeValues[i] = x[i].abs();
		}
		return magnitudeValues;
	}

	private int[] desiredElementsFFT(double[] magnitude){
		double resolution = FFT.calculateResolution(magnitude,
				Configuration.SAMPLE_FREQUENCY);
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

	// TODO: Correct implementation of cross correlation detection
	private boolean matchDetectionXCorr(double[] xCorr){
		double averageMagnitude = 0;
		boolean match;
		for (int i = 0; i < xCorr.length; i++) {
			averageMagnitude += xCorr[i];
		}
		averageMagnitude = averageMagnitude / xCorr.length;

		int center = xCorr.length / 2;
		int peak = 0;
		for (int i = (center - 3); i <= (center + 3); i++) {
			peak += xCorr[i];
		}

		peak = peak / 7;

		if(peak > averageMagnitude * 1.5){
			match = true;
		} else {
			match = false;
		}

		return match;
	}

	public static double[] copyFromIntArray(int[] source) {
		double[] dest = new double[source.length];
		for(int i=0; i<source.length; i++) {
			dest[i] = source[i];
		}
		return dest;
	}
}
