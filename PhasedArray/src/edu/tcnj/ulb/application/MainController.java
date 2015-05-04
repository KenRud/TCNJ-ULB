package edu.tcnj.ulb.application;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import static edu.tcnj.ulb.dsp.DataProcessor.SEARCH_SIGNAL;
import static edu.tcnj.ulb.dsp.DataProcessor.SAMPLE_FREQUENCY;
import edu.tcnj.ulb.daq.Recording;
import edu.tcnj.ulb.dsp.DataProcessor;
import edu.tcnj.ulb.daq.DataParser;


public class MainController {
	@FXML private Button loadButton;
	@FXML private Button recordButton;
	@FXML private LineChart searchSignal;
	@FXML private LineChart frequencyResponse;
	
	public static final int CHUNK_SIZE = 1000;
	public static final int NUM_CHANNELS = 9;
	private MainApp mainApp;
	
	@FXML
	private void initialize() {
		loadButton.setOnAction((event) -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Load a past recording");
			File file = chooser.showDialog(mainApp.getPrimaryStage());
			try{
				Recording recording = Recording.load(file.getPath());
				DataParser parser = new DataParser(recording, NUM_CHANNELS, CHUNK_SIZE);
				DataProcessor processor = new DataProcessor(parser, this);
				processor.process();
			}catch(IOException e) {
				e.printStackTrace();
			}
		});
		
		recordButton.setOnAction((event) -> {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select location to save the recording");
			File file = chooser.showSaveDialog(mainApp.getPrimaryStage());
		});

		createSearchSignalGraph();
	}
	
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	private void createSearchSignalGraph(){

		searchSignal.setTitle("Search Signal");

		XYChart.Series series = new XYChart.Series();
		series.setName("Search Signal");

		for(int i = 0; i < SEARCH_SIGNAL.length / 4; i++){
			double time = (double) i / SAMPLE_FREQUENCY;
			series.getData().add(new XYChart.Data(time, SEARCH_SIGNAL[i]));
		}

		searchSignal.getData().add(series);

	}

	public void updateFFTGraph(double[] fftPoints){

		frequencyResponse.setTitle("Frequency Response");
		frequencyResponse.getXAxis().setAutoRanging(true);
		frequencyResponse.getYAxis().setAutoRanging(true);
		XYChart.Series series = new XYChart.Series();
		series.setName("Frequency Response");

		for(int i = 0; i < fftPoints.length; i++){
			series.getData().add(new XYChart.Data(i, fftPoints[i]));
		}

		frequencyResponse.getData().add(series);
	}

}
