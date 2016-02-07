package pl.danlz.remotecontrol.samsung.gui.task;

import java.util.ResourceBundle;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.adapter.TVAuthenticationException;
import pl.danlz.remotecontrol.samsung.config.Configuration;

/**
 * Task for sending key to TV.
 *
 * @author Leszek
 */
public class SendKeyTask extends CommunicationTask {

	private Configuration config;

	private TVAdapter adapter;

	private String keyCode;

	private int delay;

	/**
	 * Creates a new instance.
	 *
	 * @param resources
	 *            resources
	 * @param config
	 *            configuration
	 * @param adapter
	 *            TV adapter
	 * @param keyCode
	 *            key code
	 */
	public SendKeyTask(ResourceBundle resources, Configuration config, TVAdapter adapter, String keyCode) {
		this(resources, config, adapter, keyCode, 0);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param resources
	 *            resources
	 * @param config
	 *            configuration
	 * @param adapter
	 *            TV adapter
	 * @param keyCode
	 *            key code
	 * @param delay
	 *            delay (in ms) added after sending the key
	 */
	public SendKeyTask(ResourceBundle resources, Configuration config, TVAdapter adapter, String keyCode, int delay) {
		super(resources);
		this.config = config;
		this.adapter = adapter;
		this.keyCode = keyCode;
		if (delay < 0) {
			throw new IllegalArgumentException("delay must be > 0");
		}
		this.delay = delay;
	}

	@Override
	protected Void call() throws Exception {
		if (!adapter.isConnected()) {
			adapter.close();
			adapter.connect(config.getTvAddress(), config.getTvPort(), config.getControllerName());
		}
		adapter.sendKey(keyCode);
		if (delay > 0) {
			Thread.sleep(delay);
		}
		return null;
	}

	@Override
	protected void failed() {
		if (getException() instanceof TVAuthenticationException) {
			Alert alert = new Alert(AlertType.INFORMATION, resources.getString("confirmation.text"));
			alert.setTitle(resources.getString("title"));
			alert.setHeaderText(resources.getString("confirmation.header"));
			alert.showAndWait();
		} else {
			super.failed();
		}
	}
}
