package pl.danlz.remotecontrol.samsung;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.danlz.remotecontrol.samsung.gui.ControllerInitializer;

/**
 * Application entry point.
 *
 * @author Leszek
 */
public class Startup extends Application {

	/**
	 * Application main method.
	 *
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ControllerInitializer.init(primaryStage);

		primaryStage.show();
	}

}
