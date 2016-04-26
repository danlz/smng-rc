package pl.danlz.remotecontrol.samsung.adapter;

/**
 * Adapter for communicating with the TV.
 *
 * @author Leszek
 */
public interface TVAdapter {

	/**
	 * Connects (and authenticates) to the TV.
	 *
	 * @param address
	 *            TV address
	 * @param port
	 *            TV port
	 * @param controllerName
	 *            controller name (displayed on TV when granting access)
	 *
	 * @throws TVAdapterException
	 *             if connection fails
	 */
	void connect(String address, int port, String controllerName) throws TVAdapterException;

	/**
	 * Sends keys to the TV.
	 *
	 * @param keyCodes
	 *            key codes
	 * @throws TVAdapterException
	 *             if send fails
	 */
	void sendKeys(String... keyCodes) throws TVAdapterException;

	/**
	 * Tells if this adapter is connected to TV.
	 *
	 * @return {@code true} if this adapter is connected; otherwise
	 *         {@code false}
	 */
	boolean isConnected();

	/**
	 * Closes connection to the TV.
	 */
	void close();
}
