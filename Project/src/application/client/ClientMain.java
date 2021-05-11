package application.client;

import application.client.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader Loader = new FXMLLoader(getClass().getResource("./fxml/loginRoom.fxml"));
			Parent Root = Loader.load();
			LoginController Controller = Loader.getController();
			Controller.setStage(primaryStage);
			
			new animatefx.animation.FadeIn(Root).play();
			Scene scene = new Scene(Root);
			scene.getStylesheets().add(getClass().getResource("../css/app.css").toString());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Login");
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}

