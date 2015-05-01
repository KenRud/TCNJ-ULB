package edu.tcnj.ulb.daq;

import java.io.IOException;

public class Recording {
	public static final int BYTES_PER_SECOND = 360000;
	public static final int BYTES_PER_MINUTE = BYTES_PER_SECOND * 60;
	public static final int DEFUALT_FILE_SIZE = BYTES_PER_MINUTE * 10;
	
	private RecordingMetaData metaData;
	private ArduinoReader reader;
	
	public static Recording create(String path) {
		return create(path, DEFUALT_FILE_SIZE);
	}
	
	public static Recording create(String path, int fileSize) {
		Recording recording = new Recording();
		recording.metaData = RecordingMetaData.create(path);
		recording.reader = new ArduinoReader(path, fileSize, recording.metaData::addRecordingFile);
		return recording;
	}
	
	public static Recording load(String path) throws IOException {
		Recording recording = new Recording();
		recording.metaData = RecordingMetaData.load(path);
		return recording;
	}
	
	private Recording() {
	}
	
	public void start() {
		if (reader == null) {
			throw new IllegalStateException(
					"Unable to start a recording that was loaded from a file");
		}
		metaData.setTimestamp();
		reader.start();
	}
	
	public void stop() throws IOException {
		if (reader == null) {
			throw new IllegalStateException(
					"Unable to stop a recording that was loaded from a file");
		}
		reader.stop();
		metaData.save();
	}
	
	public RecordingMetaData getMetaData() {
		return metaData;
	}
}
