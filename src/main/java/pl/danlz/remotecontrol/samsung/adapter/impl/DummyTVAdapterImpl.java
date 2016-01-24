package pl.danlz.remotecontrol.samsung.adapter.impl;

import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapterException;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Dummy adapter, which only logs key codes.
 *
 * @author Leszek
 */
public class DummyTVAdapterImpl implements TVAdapter {

	private static final Logger LOG = Logger.getLogger(DummyTVAdapterImpl.class);

	@Override
	public void connect(String address, int port, String controllerName) throws TVAdapterException {
		// nothing
	}

	@Override
	public void sendKey(String keyCode) throws TVAdapterException {
		LOG.info("Key code \"" + keyCode + "\"");
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public void close() {
		// nothing
	}
}
