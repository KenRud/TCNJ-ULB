package edu.tcnj.ulb.application;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import edu.tcnj.ulb.daq.Recording;

public class MainController {
	private static final String BEST_MATCH_SIGNAL_SERIES_NAME = "Best Match Signal";
	private static final String FFT_SIGNAL_SERIES_NAME = "FFT Series";
	@FXML private Button loadButton;
	@FXML private Button recordButton;
//	@FXML private LineChart<Double, Double> searchSignal;
	@FXML private LineChart<Integer, Integer> bestMatchSignal;
	@FXML private LineChart<Double, Double> fftSignal;

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
				Recording recording = Recording.load(file.getPath());
				processingTask = new ProcessingTask(recording);
				initBestMatchTimeChart(processingTask);
				initFFTSignalChart(processingTask);
				executorService.execute(processingTask);
			}catch(IOException e) {
				e.printStackTrace();
			}
		});

		recordButton.setOnAction((event) -> {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select location to save the recording");
			File file = chooser.showSaveDialog(mainApp.getPrimaryStage());
		});
//		createSearchSignalGraph();
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

//	private void createSearchSignalGraph(){
//		searchSignal.setTitle("Search Signal");
//
//		XYChart.Series<Double, Double> series = new XYChart.Series<>();
//		series.setName("Search Signal");
//
//		for(int i = 0; i < SEARCH_SIGNAL.length / 4; i++){
//			double time = (double) i / Configuration.SAMPLE_FREQUENCY;
//			series.getData().add(new XYChart.Data<>(time, SEARCH_SIGNAL[i]));
//		}
//
	// searchSignal.getData().add(series);
	//
	// }

	private void initBestMatchTimeChart(ProcessingTask task) {
		bestMatchSignal.getXAxis().setAutoRanging(true);
		bestMatchSignal.getYAxis().setAutoRanging(true);
		Series<Integer, Integer> series = new Series<>(
				BEST_MATCH_SIGNAL_SERIES_NAME, task.getResults()
						.bestMatchSignalProperty());
		bestMatchSignal.getData().clear();
		bestMatchSignal.getData().add(series);
		// for(int i = 0; i < fftPoints.length; i++){
		// series.getData().add(new XYChart.Data<>((double) i, fftPoints[i]));
		// }
//		processingTask.fftResultsProperty().stream().sequential().map(d -> {
//			return new XYChart.Data<Double, Double>(d, 3.);
//		});
//
//		series.setData(processingTask.fftResultsProperty().stream().sequential().map(d -> {
//			return new XYChart.Data<Double, Double>(d, 3.);
//		}).collect(()));
	}

	private void initFFTSignalChart(ProcessingTask task) {
		fftSignal.getXAxis().setAutoRanging(true);
		fftSignal.getYAxis().setAutoRanging(true);
		Series<Double, Double> series = new Series<>(
				FFT_SIGNAL_SERIES_NAME, task.getResults()
				.fftMatchSignalProperty());
		fftSignal.getData().clear();
		fftSignal.getData().add(series);
	}
}
