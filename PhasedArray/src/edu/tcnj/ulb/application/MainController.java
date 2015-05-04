package edu.tcnj.ulb.application;

import static edu.tcnj.ulb.dsp.DataProcessor.SEARCH_SIGNAL;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import edu.tcnj.ulb.Configuration;
import edu.tcnj.ulb.daq.Recording;

public class MainController {
	@FXML private Button loadButton;
	@FXML private Button recordButton;
	@FXML private LineChart<Double, Double> searchSignal;
	@FXML private LineChart<Double, Double> frequencyResponse;
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(10);
	private MainApp mainApp;
	
	@FXML
	private void initialize() {
		loadButton.setOnAction((event) -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Load a past recording");
			File file = chooser.showDialog(mainApp.getPrimaryStage());
			try{
				Recording recording = Recording.load(file.getPath());
				ProcessingTask task = new ProcessingTask(recording);
				executorService.execute(task);
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

		XYChart.Series<Double, Double> series = new XYChart.Series<>();
		series.setName("Search Signal");

		for(int i = 0; i < SEARCH_SIGNAL.length / 4; i++){
			double time = (double) i / Configuration.SAMPLE_FREQUENCY;
			series.getData().add(new XYChart.Data<>(time, SEARCH_SIGNAL[i]));
		}

		searchSignal.getData().add(series);

	}

	public void updateFFTGraph(double[] fftPoints){
		frequencyResponse.setTitle("Frequency Response");
		frequencyResponse.getXAxis().setAutoRanging(true);
		frequencyResponse.getYAxis().setAutoRanging(true);
		XYChart.Series<Double, Double> series = new XYChart.Series<>();
		series.setName("Frequency Response");

		for(int i = 0; i < fftPoints.length; i++){
			series.getData().add(new XYChart.Data<>((double) i, fftPoints[i]));
		}

		frequencyResponse.getData().add(series);
	}
}
