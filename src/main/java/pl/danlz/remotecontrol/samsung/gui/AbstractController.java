package pl.danlz.remotecontrol.samsung.gui;

import javafx.stage.Stage;

/**
 * Base class for controllers.
 *
 * @author Leszek
 */
public class AbstractController {

	protected Stage stage;

	/**
	 * Called after the FXML root object is attached the scene and scene is
	 * attached to stage.
	 *
	 * @param stage
	 *            stage used by this controller
	 */
	protected void initStage(Stage stage) {
	}

	/**
	 * Shows the stage associated with this controller.
	 */
	public void show() {
		stage.show();
	}

	/**
	 * Hides the stage associated with this controller.
	 */
	public void hide() {
		stage.hide();
	}
}
