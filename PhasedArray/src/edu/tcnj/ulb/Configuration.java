package edu.tcnj.ulb;

public class Configuration {
	public static final int CHUNK_SIZE = 1000;
	public static final int NUM_CHANNELS = 9;
	public static final int BYTES_PER_SECOND = 360000;
	public static final int BYTES_PER_MINUTE = BYTES_PER_SECOND * 60;
	public static final int TRANSMITTER_FREQUENCY = 6300;
	public static final int SAMPLE_FREQUENCY = 20_000;
	public static final int DC_OFFSET = 2048;
}
