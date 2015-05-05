package edu.tcnj.ulb.application;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart.Data;

public class ProcessingResults {
	private final int samplingFrequency;
	
	private final ReadOnlyListWrapper<Data<Integer, Integer>> matchSignal = 
			new ReadOnlyListWrapper<>(this, "matchSignal", FXCollections.observableArrayList());
	private ArrayList<Data<Integer, Integer>> outgoingMatchSignal = new ArrayList<>();
	
	public ProcessingResults(int samplingFrequency) {
		this.samplingFrequency = samplingFrequency;
	}

	public final ReadOnlyListProperty<Data<Integer, Integer>> bestMatchSignalProperty() {
		return matchSignal.getReadOnlyProperty();
	}
	
	public void setBestMatchSignal(Instant startTime, int[] data) {
		outgoingMatchSignal = new ArrayList<>(data.length);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
				"yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
		for (int i = 0; i < data.length; i++) {
//			String time = formatter.format(startTime
//					.plusNanos((long) (1e9 * i / samplingFrequency)));
			outgoingMatchSignal.add(new Data<Integer, Integer>(i, data[i]));
		}
	}
	
	public void render() {
		Platform.runLater(()-> {
			matchSignal.get().setAll(outgoingMatchSignal);
		});
	}
}
