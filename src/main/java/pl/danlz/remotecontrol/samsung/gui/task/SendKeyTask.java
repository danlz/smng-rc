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

	private int quietPeried;

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
	 * @param quietPeriod
	 *            delay (in ms) added after sending the key
	 */
	public SendKeyTask(ResourceBundle resources, Configuration config, TVAdapter adapter, String keyCode,
			int quietPeriod) {
		super(resources);
		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}
		this.config = config;
		if (adapter == null) {
			throw new IllegalArgumentException("adapter must not be null");
		}
		this.adapter = adapter;
		if (keyCode == null) {
			throw new IllegalArgumentException("keyCode must not be null");
		}
		this.keyCode = keyCode;
		if (quietPeriod < 0) {
			throw new IllegalArgumentException("quietPeriod must be > 0");
		}
		this.quietPeried = quietPeriod;
	}

	@Override
	protected Void call() throws Exception {
		if (!adapter.isConnected()) {
			adapter.close();
			adapter.connect(config.getTvAddress(), config.getTvPort(), config.getControllerName());
		}
		adapter.sendKey(keyCode);
		if (quietPeried > 0) {
			Thread.sleep(quietPeried);
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
