package pl.danlz.remotecontrol.samsung.gui.task;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Base class for tasks for communication with TV.
 *
 * @author Leszek
 */
public abstract class CommunicationTask extends Task<Void> {

	private static final Logger LOG = Logger.getLogger(CommunicationTask.class);

	@Override
	protected void failed() {
		LOG.error("Communication failed", getException());
		Alert alert = new Alert(AlertType.ERROR, getException().getMessage());
		alert.setTitle("Samsung Remote Control");
		alert.setHeaderText("Communication error");
		alert.showAndWait();
	}
}
