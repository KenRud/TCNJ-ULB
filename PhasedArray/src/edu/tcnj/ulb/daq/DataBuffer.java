package edu.tcnj.ulb.daq;

import java.nio.ByteBuffer;

public class DataBuffer {
	private ByteBuffer buffer;
	private ArduinoReader reader;
	
	public DataBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}
	
	public DataBuffer(ArduinoReader reader) {
		this.reader = reader;
		buffer = reader.getUpdatedReadBuffer();
	}
	
	public short get(int index) throws InterruptedException {
		if (reader != null) {
			reader.waitForAvailable(index);
			buffer = reader.getUpdatedReadBuffer();
		}
		return buffer.getShort(index);
	}
	
	public int limit() {
		if (reader != null) {
			buffer = reader.getUpdatedReadBuffer();
		}
		return buffer.limit();
	}
}
