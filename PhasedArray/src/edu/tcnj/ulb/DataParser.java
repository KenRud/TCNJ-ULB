package edu.tcnj.ulb;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DataParser {
	private final int numChannels;
	private final int chunkSize;

	private ByteBuffer buffer;
	private ArduinoReader arduinoReader;
	
	public DataParser(ByteBuffer buffer, int numChannels, int chunkSize) {
		this.buffer = buffer;
		this.numChannels = numChannels;
		this.chunkSize = chunkSize;
	}
	
	public DataParser(ArduinoReader arduinoReader, int numChannels, int chunkSize) {
		this.arduinoReader = arduinoReader;
		this.buffer = arduinoReader.getUpdatedReadBuffer();
		this.numChannels = numChannels;
		this.chunkSize = chunkSize;
	}
	
	public ParsedChannel getChannel(int channel) {
		if (channel >= numChannels) {
			throw new IndexOutOfBoundsException(String.format(
					"Cannot get channel %d. Channels range from 0 to %d.",
					channel, numChannels - 1));
		}
		return new ParsedChannel(channel);
	}
	
	public class ParsedChannel implements Iterable<Short[]> {
		private int chunkIndex;

		public ParsedChannel(int channel) {
			chunkIndex = channel * chunkSize;
		}
		
		@Override
		public Iterator<Short[]> iterator() {
			return new Iterator<Short[]>() {
				@Override
				public boolean hasNext() {
					return chunkIndex + chunkSize <= buffer.capacity();
				}

				@Override
				public Short[] next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					waitForUpdatedBuffer();
					Short[] chunk = new Short[chunkSize];
					for (int i = 0; i < chunkSize; i += 2) {
						chunk[i] = buffer.getShort();
					}
					chunkIndex += (numChannels * chunkSize);
					return chunk;
				}

			};
		}
		
		private void waitForUpdatedBuffer() {
			if (arduinoReader != null) {
				try {
					arduinoReader.waitForAvailable(chunkIndex + chunkSize);
					buffer = arduinoReader.getUpdatedReadBuffer();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
