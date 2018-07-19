package land.spooky.media;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * @author Daniel Gelber
 * @version 1.0
 * Created 2018-07-14
 * Last Modified 2018-07-16
 *
 * This application is a media center for people to put their movies in.
 * It comes with built in tools to gather the data of movies from IMDb.
 */
public class MediaCenter extends Application {

	/**
	 * Called when the application is launched, it loads all the fonts into memory.
	 */
	@Override
    public void init() {

        Font.loadFont(MediaCenter.class.getResource("/fonts/Raleway-Light.ttf").toExternalForm(), 50);
        Font.loadFont(MediaCenter.class.getResource("/fonts/Raleway-Bold.ttf").toExternalForm(), 50);
        Font.loadFont(MediaCenter.class.getResource("/fonts/Raleway-Thin.ttf").toExternalForm(), 50);
        Font.loadFont(MediaCenter.class.getResource("/fonts/Raleway-Medium.ttf").toExternalForm(), 50);

    }

	/**
	 * Launch the application.
	 * @param primaryStage main window.
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {

		Parent root = FXMLLoader.load(getClass().getResource("/fxml/MediaCenter.fxml"));
	    Scene scene = new Scene(root, 1600, 900);
        scene.getStylesheets().add("css/style.css");
		primaryStage.setScene(scene);
        primaryStage.setMinWidth(1500);
        primaryStage.setMinHeight(800);
		primaryStage.show();

	}

	/**
	 * Exit gracefully. Remove this method to get a stack trace in case
	 * of modifications to the code.
	 */
	@Override
	public void stop() {

		System.exit(0);

	}

	/**
	 * Main method.
	 * @param args unused.
	 */
	public static void main(String[] args) {

		launch(args);

	}
}
