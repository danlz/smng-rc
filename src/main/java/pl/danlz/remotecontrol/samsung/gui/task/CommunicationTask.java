package pl.danlz.remotecontrol.samsung.gui.task;

import java.util.ResourceBundle;

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
	
	protected ResourceBundle resources;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param resources resources
	 */
	public CommunicationTask(ResourceBundle resources) {
		this.resources = resources;
	}

	@Override
	protected void failed() {
		LOG.error("Communication failed", getException());
		Alert alert = new Alert(AlertType.ERROR, getException().getMessage());
		alert.setTitle(resources.getString("communicationError.title"));
		alert.setHeaderText(resources.getString("communication.error"));
		alert.showAndWait();
	}
}
