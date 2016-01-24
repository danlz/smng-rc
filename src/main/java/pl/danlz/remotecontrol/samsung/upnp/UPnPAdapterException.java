package pl.danlz.remotecontrol.samsung.upnp;

/**
 * Thrown when there is a problem in {@link UPnPAdapter}.
 *
 * @author Leszek
 */
public class UPnPAdapterException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception.
	 *
	 * @param message
	 *            the detail message
	 */
	public UPnPAdapterException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception.
	 *
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the cause
	 */
	public UPnPAdapterException(String message, Throwable cause) {
		super(message, cause);
	}

}
