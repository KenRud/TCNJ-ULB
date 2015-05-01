package edu.tcnj.ulb.daq;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * Reads the data from the Arduino. The Arduino must be on port /dev/ttyAMC[0-99] or COM[0-99].
 * Before running on Linux be sure to run the following command:
 * <code>stty -F /dev/ttyACM[0-99] sane raw</code>
 * @author kruddick
 *
 */
public class ArduinoReader implements SerialPortEventListener, Closeable {
	private static int BAUD_RATE = SerialPort.BAUDRATE_256000;
	private static int DATA_BITS = SerialPort.DATABITS_8;
	private static int STOP_BITS = SerialPort.STOPBITS_1;
	private static int PARITY = SerialPort.PARITY_NONE;
	private static int SERIAL_EVENT_MASK = SerialPort.MASK_RXCHAR
			+ SerialPort.MASK_CTS + SerialPort.MASK_DSR;

	private final SerialPort port;
	private final String path;
	private final int fileSize;
	private final Consumer<String> newFileListener;
	
	private Object available = new Object();
	private volatile int waitPosition;
	private volatile int bufferPosition;
	
	private RandomAccessFile file;
	private MappedByteBuffer fileBuffer;
	
	public ArduinoReader(String path, int fileSize, Consumer<String> listener) {
		this.path = path;
		this.fileSize = fileSize;
		this.newFileListener = listener;
		openNewFile();
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
	
	public void stop() {
//		writePortDataToFile();
		close();
	}
	
	public ByteBuffer getUpdatedReadBuffer() {
		ByteBuffer buffer = fileBuffer.asReadOnlyBuffer();
		buffer.flip();
		return buffer;
	}
	
	public void waitForAvailable(int position) throws InterruptedException {
		if (position < 0 || position >= fileBuffer.capacity()) {
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
			writePortDataToFile();
		}
	}

	private void writePortDataToFile() {
		try {
			synchronized (available) {
				int remaining = fileBuffer.remaining();
				if (remaining >= port.getInputBufferBytesCount()) {
					// This buffer can fit all the incoming bytes
					fileBuffer.put(port.readBytes());
				} else {
					// Read as many bytes as possible and then continue with a new file
					fileBuffer.put(port.readBytes(remaining));
					openNewFile();
					// Read remaining data
					if (port.getInputBufferBytesCount() > 0) {
						fileBuffer.put(port.readBytes());
					}
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
	
	private void openNewFile() {
		String filename = UUID.randomUUID().toString();
		String filepath = Paths.get(path, filename).toString();
		try {
			// Close the currently opened file
			if (file != null) {
				file.close();
			}

			// Attempt to open a new memory-mapped file
			file = new RandomAccessFile(filepath, "rw");
			fileBuffer = file.getChannel().map(MapMode.READ_WRITE, 0, fileSize);
			
			// Provide the new file name to the listener
			if (newFileListener != null) {
				newFileListener.accept(filename);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SerialPort openArduinoPort() {
		String[] portNames = SerialPortList.getPortNames(Pattern
				.compile("(ttyACM|COM)[0-9]{1,2}"));
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
