package pl.danlz.remotecontrol.samsung.gui.task;

import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
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
	 * @param config
	 *            configuration
	 * @param adapter
	 *            TV adapter
	 * @param keyCode
	 *            key code
	 */
	public SendKeyTask(Configuration config, TVAdapter adapter, String keyCode) {
		this(config, adapter, keyCode, 0);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param config
	 *            configuration
	 * @param adapter
	 *            TV adapter
	 * @param keyCode
	 *            key code
	 * @param delay
	 *            delay (in ms) added after sending the key
	 */
	public SendKeyTask(Configuration config, TVAdapter adapter, String keyCode, int delay) {
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
}
