package edu.tcnj.ulb;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class DataParser {
	private final ByteBuffer buffer;
	private final int numChannels;
	private final int chunkSize;

	public DataParser(ByteBuffer buffer, int numChannels, int chunkSize) {
		this.buffer = buffer;
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
					Short[] chunk = new Short[chunkSize];
					for (int i = 0; i < chunkSize; i += 2) {
						chunk[i] = buffer.getShort(chunkIndex + i);
					}
					chunkIndex += (numChannels * chunkSize);
					return chunk;
				}

			};
		}
	}
}
