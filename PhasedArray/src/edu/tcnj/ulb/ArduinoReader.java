package edu.tcnj.ulb;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.regex.Pattern;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * Reads the data from the Arduino. The Arduino must be on port /dev/ttyAMC1 or COM1.
 * Before running on Linux be sure to run the following command:
 * <code>stty -F /dev/ttyACM1 sane raw</code>
 * @author kruddick
 *
 */
public class ArduinoReader implements SerialPortEventListener, Closeable {
	public static final int BYTES_PER_SECOND = 360000;
	public static final int BYTES_PER_MINUTE = BYTES_PER_SECOND * 60;
	
//	private static int ARDUINO_CHANNEL_BUFFER_SIZE = 1000;	// Bytes
//	private static int ARDUINO_BUFFER_SIZE = 9 * ARDUINO_CHANNEL_BUFFER_SIZE;
//	private static int ARDUINO_BUFFER_SIZE = 4095;
	private static int BAUD_RATE = SerialPort.BAUDRATE_256000;
	private static int DATA_BITS = SerialPort.DATABITS_8;
	private static int STOP_BITS = SerialPort.STOPBITS_1;
	private static int PARITY = SerialPort.PARITY_NONE;
	private static int SERIAL_EVENT_MASK = SerialPort.MASK_RXCHAR
			+ SerialPort.MASK_CTS + SerialPort.MASK_DSR;

	private final SerialPort port;
	private final RandomAccessFile file;
	private final MappedByteBuffer fileBuffer;
	
	private Object available = new Object();
	private volatile boolean hasRemaining = true;
	private volatile int waitPosition;
	private volatile int bufferPosition;
	
	public ArduinoReader(String filename, int size) throws IOException {
		file = new RandomAccessFile(filename, "rw");
		fileBuffer = file.getChannel().map(MapMode.READ_WRITE, 0, size);
		port = openArduinoPort();
	}

	public void start() {
		// Attempts to add the event listener
		try {
			port.addEventListener(this);
		} catch (SerialPortException e) {
			e.printStackTrace();
			close();
			throw new UnableToConnectToArduinoException();
		}
	}
	
	public boolean hasRemaining() {
		return hasRemaining;
	}
	
	public ByteBuffer getUpdatedReadBuffer() {
		ByteBuffer buffer = fileBuffer.asReadOnlyBuffer();
		buffer.flip();
		return buffer;
	}
	
	public void waitForAvailable(int position) throws InterruptedException {
		if (position >= fileBuffer.capacity()) {
			throw new IndexOutOfBoundsException();
		}
		
		synchronized (available) {
			waitPosition = position;
			while (bufferPosition < position) {
				available.wait();
			}
		}
	}

	public void serialEvent(SerialPortEvent event) {
		if (event.isRXCHAR()) {
			try {
				synchronized (available) {
					int remaining = fileBuffer.remaining();
					if (remaining >= event.getEventValue()) {
						fileBuffer.put(port.readBytes());
					} else {
						fileBuffer.put(port.readBytes(remaining));
						hasRemaining = false;	
					}
					bufferPosition = fileBuffer.position();
					if (waitPosition <= bufferPosition) {
						waitPosition = fileBuffer.capacity();
						available.notifyAll();
					}
				}
			} catch (SerialPortException ex) {
				throw new ArduinoReadException();
			}
		}
	}

	private SerialPort openArduinoPort() {
		String[] portNames = SerialPortList.getPortNames(Pattern
				.compile("(ttyACM|COM)[0-9]{1,}"));
		for (int i = portNames.length - 1; i >= 0; i--) {
			SerialPort port = new SerialPort(portNames[i]);
			System.out.println(portNames[i]);
			try {
				port.openPort();
				port.setDTR(true);
				port.setParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
				port.setEventsMask(SERIAL_EVENT_MASK);
				return port;
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
		}
		throw new UnableToConnectToArduinoException();
	}
	
	@Override
	public void close() {
		if (port != null && port.isOpened()) {
			try {
				port.closePort();
				file.close();
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static class UnableToConnectToArduinoException extends
			RuntimeException {
		private static final long serialVersionUID = 3123099754492639970L;

		public UnableToConnectToArduinoException() {
			super("Arduino device was either not found or a connection could not be established");
		}

		public UnableToConnectToArduinoException(String message) {
			super(message);
		}
	}
	
	public static class ArduinoReadException extends RuntimeException {
		private static final long serialVersionUID = -2471989466375285876L;
	}
}
