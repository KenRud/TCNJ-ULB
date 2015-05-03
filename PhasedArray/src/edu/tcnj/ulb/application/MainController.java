package edu.tcnj.ulb.application;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.scene.chart.LineChart;
import edu.tcnj.ulb.dsp.DataProcessor;

public class MainController {
	@FXML private Button loadButton;
	@FXML private Button recordButton;
	@FXML private LineChart searchSignal;
	
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
	}
	
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
