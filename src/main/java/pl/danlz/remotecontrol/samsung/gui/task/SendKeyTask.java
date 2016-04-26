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

	private String[] keyCodes;

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
	 * @param keyCodes
	 *            key codes
	 */
	public SendKeyTask(ResourceBundle resources, Configuration config, TVAdapter adapter, String keyCodes) {
		this(resources, config, adapter, 0, keyCodes);
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
	 * @param quietPeriod
	 *            delay (in ms) added after sending the key
	 * @param keyCodes
	 *            key codes
	 */
	public SendKeyTask(ResourceBundle resources, Configuration config, TVAdapter adapter, int quietPeriod,
			String... keyCodes) {
		super(resources);
		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}
		this.config = config;
		if (adapter == null) {
			throw new IllegalArgumentException("adapter must not be null");
		}
		this.adapter = adapter;
		if (quietPeriod < 0) {
			throw new IllegalArgumentException("quietPeriod must be > 0");
		}
		this.quietPeried = quietPeriod;
		if (keyCodes == null) {
			throw new IllegalArgumentException("keyCodes must not be null");
		}
		this.keyCodes = keyCodes;
	}

	@Override
	protected Void call() throws Exception {
		if (!adapter.isConnected()) {
			adapter.close();
			adapter.connect(config.getTvAddress(), config.getTvPort(), config.getControllerName());
		}
		adapter.sendKeys(keyCodes);
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
