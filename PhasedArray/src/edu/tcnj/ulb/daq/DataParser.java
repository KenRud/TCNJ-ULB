package edu.tcnj.ulb.daq;

import java.nio.ByteBuffer;

public class DataParser {
	private final int numChannels;
	private final int chunkSize;
	private final int chunkSeparation;

	private ByteBuffer buffer;
	private ArduinoReader arduinoReader;
	
	public DataParser(ByteBuffer buffer, int numChannels, int chunkSize) {
		this.buffer = buffer;
		this.numChannels = numChannels;
		this.chunkSize = chunkSize;
		chunkSeparation = numChannels * chunkSize;
	}
	
	public DataParser(ArduinoReader reader, int numChannels, int chunkSize) {
		arduinoReader = reader;
		buffer = reader.getUpdatedReadBuffer();
		this.numChannels = numChannels;
		this.chunkSize = chunkSize;
		chunkSeparation = numChannels * chunkSize;
	}
	
	public ParsedChannel getChannel(int channel) {
		if (channel >= numChannels) {
			throw new IndexOutOfBoundsException(String.format(
					"Cannot get channel %d. Channels range from 0 to %d.",
					channel, numChannels - 1));
		}
		if (arduinoReader != null) {
			return new LiveParsedChannel(channel);
		}
		return new ParsedChannel(channel);
	}
	
	public int numChannels() {
		return numChannels;
	}
	public int chunkSize(){
		return chunkSize;
	}
	
	public class ParsedChannel {
		private final int channelOffset;

		public ParsedChannel(int channel) {
			channelOffset = channel * chunkSize;
		}
		
		public short get(int index) {
			int bufferIndex = getBufferIndex(index);
			if (bufferIndex < 0 || bufferIndex >= buffer.limit()) {
				throw new IndexOutOfBoundsException();
			}
			return buffer.getShort(bufferIndex);
		}
		
		public short[] get(int start, int length) {
			short[] data = new short[length];
			// TODO Determine if this is as fast as a bulk ByteBuffer.get
			for (int i = 0; i < length; i++) {
				data[i] = get(i + start);
			}
			return data;
		}
		
		protected int getBufferIndex(int index) {
			return index + channelOffset + chunkSeparation * index / chunkSize;
		}
	}
	
	private class LiveParsedChannel extends ParsedChannel {

		public LiveParsedChannel(int channel) {
			super(channel);
		}
		
		@Override
		public short get(int index) {
			int bufferIndex = getBufferIndex(index);
			if (bufferIndex > buffer.limit()) {
				try {
					arduinoReader.waitForAvailable(bufferIndex);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer = arduinoReader.getUpdatedReadBuffer();
			}
			return super.get(index);
		}
		
		@Override
		public short[] get(int start, int length) {
			int bufferIndex = getBufferIndex(start + length);
			if (bufferIndex > buffer.limit()) {
				try {
					arduinoReader.waitForAvailable(bufferIndex);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer = arduinoReader.getUpdatedReadBuffer();
			}
			return super.get(start, length);
		}
	}
}
