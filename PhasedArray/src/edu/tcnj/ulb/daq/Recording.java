package edu.tcnj.ulb.daq;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.util.List;

import edu.tcnj.ulb.Configuration;

public class Recording {
	public static final int DEFUALT_FILE_SIZE = Configuration.BYTES_PER_MINUTE * 5;
	
	private final String path;
	private RecordingMetaData metaData;
	
	// Active recording mode
	private ArduinoReader reader;
	
	// Playback mode
	private int currentFileIndex = -1;
	private MappedByteBuffer currentBuffer;
	
	public static Recording create(String path) {
		return create(path, DEFUALT_FILE_SIZE);
	}

	public static Recording create(File path) {
		return create(path.toString(), DEFUALT_FILE_SIZE);
	}
	
	public static Recording create(String path, int fileSize) {
		Recording recording = new Recording(path);
		recording.metaData = RecordingMetaData.create(path, fileSize);
		recording.reader = new ArduinoReader(path, fileSize, recording.metaData::addRecordingFile);
		return recording;
	}
	
	public static Recording load(String path) throws IOException {
		Recording recording = new Recording(path);
		recording.metaData = RecordingMetaData.load(path);
		return recording;
	}
	
	private Recording(String path) {
		this.path = path;
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
		int position = reader.stop();
		metaData.setStopPosition(position);
		metaData.save();
	}
	
	public RecordingMetaData getMetaData() {
		return metaData;
	}

	public long size() {
		return (metaData.getFilenames().size() - 1) * metaData.fileSize()
				+ metaData.getStopPosition();
	}
	
	public boolean isValid(long index) {
		return index < size();
	}
	
	public short get(long index) {
		int fileIndex = (int) (index / metaData.fileSize());
		int bufferIndex = (int) (index - fileIndex * metaData.fileSize());
		MappedByteBuffer buffer;
		try {
			buffer = getFileBuffer(fileIndex);
		} catch (IOException e) {
			// TODO Create a new error type
			throw new RuntimeException("An error occured in reading the recording");
		}
		return buffer.getShort(bufferIndex);
	}
	
	private MappedByteBuffer getFileBuffer(int fileIndex) throws IOException {
		if (fileIndex != currentFileIndex) {
			// Update the current buffer to the requested file
			List<String> filenames = metaData.getFilenames();
			String filepath = Paths.get(path, metaData.getFilenames().get(fileIndex)).toString();
				
			try (RandomAccessFile file = new RandomAccessFile(filepath, "r")) {
				int length;
				if (fileIndex == filenames.size() - 1) {
					length = metaData.getStopPosition();
				} else {
					length = (int) file.length();
				}
				currentBuffer = file.getChannel().map(
						MapMode.READ_ONLY, 0, length);
			}
		}
		return currentBuffer;
	}
}
