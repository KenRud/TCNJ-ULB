package edu.tcnj.ulb.daq;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordingMetaData implements Serializable {
	private static final long serialVersionUID = -6125741047540796622L;
	private static final String FILE_NAME = ".recording";
	
	private transient String path;
	
	private Instant timestamp;
	private ArrayList<String> filenames;
	
	public static RecordingMetaData create(String path) {
		return new RecordingMetaData(path);
	}
	
	public static RecordingMetaData load(String path) throws IOException {
		FileInputStream fileIn = new FileInputStream(Paths.get(path, FILE_NAME).toString());
        ObjectInputStream in = new ObjectInputStream(fileIn);
        RecordingMetaData meta = null;
		try {
			meta = (RecordingMetaData) in.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        in.close();
        fileIn.close();
		return meta;
	}
	
	private RecordingMetaData(String path) {
		this.path = path;
		filenames = new ArrayList<>();
	}
	
	public Instant getTimestamp() {
		return timestamp;
	}
	
	public List<String> getFilenames() {
		return Collections.unmodifiableList(filenames);
	}
	
	protected void setTimestamp() {
		if (timestamp != null) {
			throw new IllegalStateException("Timestamps cannot be changed once set");
		}
		timestamp = Instant.now();
	}
	
	protected void addRecordingFile(String filename) {
		filenames.add(filename);
		try {
			save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void save() throws IOException {
		FileOutputStream fileOut = new FileOutputStream(Paths.get(path, FILE_NAME).toString());
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
	}
}
