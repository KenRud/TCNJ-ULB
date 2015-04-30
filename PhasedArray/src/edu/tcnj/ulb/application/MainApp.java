package edu.tcnj.ulb.application;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {
	private Stage primaryStage;
	private Pane rootLayout;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("TCNJ ULB - Phased Array");
		initMainScene();
	}
	
	public void initMainScene() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL r = MainApp.class.getResource("/fxml/Main.fxml");
			System.out.println(r);
			loader.setLocation(r);
			rootLayout = loader.load();
			
			MainController controller = loader.getController();
			controller.setMainApp(this);
			
			Scene scene = new Scene(rootLayout);
			scene.getStylesheets().add(
					getClass().getResource("/styles/application.css")
					.toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
