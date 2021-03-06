package edu.tcnj.ulb.application;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart.Data;

public class ProcessingResults {
	private static final int RENDER_WAIT_MILLIS = 100;

	private static final int RENDER_QUEUE_SIZE = 2;

	private final int samplingFrequency;
	private final Object renderSync = new Object();
	private volatile int renderCount;
	
	private final ReadOnlyListWrapper<Data<Integer, Double>> matchSignal =
			new ReadOnlyListWrapper<>(this, "matchSignal", FXCollections.observableArrayList());
	private ArrayList<Data<Integer, Double>> outgoingMatchSignal = new ArrayList<>();

	private final ReadOnlyListWrapper<Data<Double, Double>> fftSignal =
			new ReadOnlyListWrapper<>(this, "fftSignal", FXCollections.observableArrayList());
	private ArrayList<Data<Double, Double>> outgoingFFTSignal = new ArrayList<>();
	
	public ProcessingResults(int samplingFrequency) {
		this.samplingFrequency = samplingFrequency;
	}

	public final ReadOnlyListProperty<Data<Integer, Double>> bestMatchSignalProperty() {
		return matchSignal.getReadOnlyProperty();
	}

	public final ReadOnlyListProperty<Data<Double, Double>> fftMatchSignalProperty() {
		return fftSignal.getReadOnlyProperty();
	}
	
	public void setBestMatchSignal(Instant startTime, double[] data) {
		outgoingMatchSignal = new ArrayList<>(data.length);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
				"yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
		for (int i = 0; i < data.length; i++) {
//			String time = formatter.format(startTime
//					.plusNanos((long) (1e9 * i / samplingFrequency)));
			outgoingMatchSignal.add(new Data<>(i, data[i]));
		}
	}

	public void setFFTSignal(double[] freqResponse) {
		int halfLength = freqResponse.length / 2;
		outgoingFFTSignal = new ArrayList<>(halfLength);
		for (int i = 5; i < halfLength; i++) {
			outgoingFFTSignal.add(new Data<>((double) i * samplingFrequency
					/ freqResponse.length, freqResponse[i]));
		}
	}
	
	public void render() throws InterruptedException {
		synchronized (renderSync) {
			renderCount++;

			final List<Data<Integer, Double>> outgoingMatchSignal = new ArrayList<>(
					this.outgoingMatchSignal);
			final List<Data<Double, Double>> outgoingFFTSignal = new ArrayList<>(
					this.outgoingFFTSignal);

			Platform.runLater(()-> {
				synchronized (renderSync) {
					renderCount--;
					
					matchSignal.get().setAll(outgoingMatchSignal);
					fftSignal.get().setAll(outgoingFFTSignal);
					
					renderSync.notify();
				}
			});
			
			// Wait for render to complete
			renderSync.wait();
			Thread.sleep(RENDER_WAIT_MILLIS);
		}
	}
}
