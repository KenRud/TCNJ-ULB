package edu.tcnj.ulb.dsp;

import edu.tcnj.ulb.daq.DataParser;

public class DataProcessor {
	private final DataParser parser;
	public static final int WINDOW_SIZE = 512;
	public static final int TRANSMITTER_FREQUENCY = 6300;

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
		}

		// TODO: send this window off for processing
		computeFFT(chunkWindow[1]);

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
