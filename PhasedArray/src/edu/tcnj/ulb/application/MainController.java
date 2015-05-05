package edu.tcnj.ulb.application;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.tcnj.ulb.Configuration;
import edu.tcnj.ulb.dsp.DataProcessor;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import edu.tcnj.ulb.daq.Recording;

public class MainController {
	private static final String BEST_MATCH_SIGNAL_SERIES_NAME = "Best Match Signal";
	private static final String FFT_SIGNAL_SERIES_NAME = "FFT Series";
	@FXML private Button loadButton;
	@FXML private Button recordButton;
	@FXML private Button stopButton;
	@FXML private LineChart<Double, Double> searchSignal;
	@FXML private LineChart<Integer, Double> bestMatchSignal;
	@FXML private LineChart<Double, Double> fftSignal;
	@FXML private NumberAxis fftSignalX;
	@FXML private NumberAxis fftSignalY;

	Recording recording;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private MainApp mainApp;
	private ProcessingTask processingTask;

	@FXML
	private void initialize() {
		loadButton.setOnAction((event) -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Load a past recording");
			File file = chooser.showDialog(mainApp.getPrimaryStage());
			try{
				recording = Recording.load(file.getPath());
				processingTask = new ProcessingTask(recording);
				initBestMatchTimeChart(processingTask);
				initFFTSignalChart(processingTask);
				executorService.execute(processingTask);
			}catch(IOException e) {
				e.printStackTrace();
			}
		});

		recordButton.setOnAction((event) -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select location to save the recording");
			File file = chooser.showDialog(mainApp.getPrimaryStage());

			recording = Recording.create(file);
			recording.start();
		});

		stopButton.setOnAction((event) -> {
			try{
				recording.stop();
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Recording Finished");
				alert.setHeaderText("Your Recording Has Finished!");
				alert.setContentText("The recording has been saved under the directory chosen by you.");

				alert.showAndWait();
			}catch(IOException e){
				e.printStackTrace();
			}

		});


		createSearchSignalGraph();
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	private void createSearchSignalGraph(){
		searchSignal.getXAxis().setAutoRanging(true);
		searchSignal.getYAxis().setAutoRanging(true);
		searchSignal.setTitle("Search Signal");

		XYChart.Series<Double, Double> series = new XYChart.Series<>();
		series.setName("Search Signal");

		for(int i = 0; i < DataProcessor.SEARCH_SIGNAL.length/4; i++){
			double time = (double) i / Configuration.SAMPLE_FREQUENCY;
			series.getData().add(new XYChart.Data<>(time, DataProcessor.SEARCH_SIGNAL[i]));
		}

	 searchSignal.getData().add(series);

	 }

	private void initBestMatchTimeChart(ProcessingTask task) {
		Series<Integer, Double> series = new Series<>(
				BEST_MATCH_SIGNAL_SERIES_NAME, task.getResults()
						.bestMatchSignalProperty());
		bestMatchSignal.getData().clear();
		bestMatchSignal.getData().add(series);
	}

	private void initFFTSignalChart(ProcessingTask task) {
		Series<Double, Double> series = new Series<>(
				FFT_SIGNAL_SERIES_NAME, task.getResults()
				.fftMatchSignalProperty());
		fftSignal.getData().clear();
		fftSignal.getData().add(series);
	}
}
