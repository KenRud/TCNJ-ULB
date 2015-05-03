package edu.tcnj.ulb.application;

import java.io.File;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import static edu.tcnj.ulb.dsp.DataProcessor.SEARCH_SIGNAL;
import static edu.tcnj.ulb.dsp.DataProcessor.SAMPLE_FREQUENCY;

public class MainController {
	@FXML private Button loadButton;
	@FXML private Button recordButton;
	@FXML private LineChart searchSignal;
	@FXML private static LineChart frequencyResponse;
	
	private MainApp mainApp;
	
	@FXML
	private void initialize() {
		loadButton.setOnAction((event) -> {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Load a past recording");
			File file = chooser.showOpenDialog(mainApp.getPrimaryStage());
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

}
