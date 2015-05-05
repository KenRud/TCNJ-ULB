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

	private final ReadOnlyListWrapper<Data<Double, Double>> fftSignal =
			new ReadOnlyListWrapper<>(this, "fftSignal", FXCollections.observableArrayList());
	private ArrayList<Data<Double, Double>> outgoingFFTSignal = new ArrayList<>();
	
	public ProcessingResults(int samplingFrequency) {
		this.samplingFrequency = samplingFrequency;
	}

	public final ReadOnlyListProperty<Data<Integer, Integer>> bestMatchSignalProperty() {
		return matchSignal.getReadOnlyProperty();
	}

	public final ReadOnlyListProperty<Data<Double, Double>> fftMatchSignalProperty() {
		return fftSignal.getReadOnlyProperty();
	}
	
	public void setBestMatchSignal(Instant startTime, int[] data) {
		outgoingMatchSignal = new ArrayList<>(data.length);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
				"yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
		for (int i = 0; i < data.length; i++) {
//			String time = formatter.format(startTime
//					.plusNanos((long) (1e9 * i / samplingFrequency)));
			outgoingMatchSignal.add(new Data<>(i, data[i]));
		}
	}

	public void setFFTSignal(double[] freqResponse){

		outgoingFFTSignal = new ArrayList<>(freqResponse.length);
		for (int i = 0; i < freqResponse.length; i++) {
			fftSignal.add(new Data<>((double)i, freqResponse[i]));
		}


	}
	
	public void render() {
		Platform.runLater(()-> {
			matchSignal.get().setAll(outgoingMatchSignal);
			fftSignal.get().setAll(outgoingFFTSignal);
		});
	}
}
