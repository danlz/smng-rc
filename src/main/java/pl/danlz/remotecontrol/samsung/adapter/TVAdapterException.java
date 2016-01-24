package pl.danlz.remotecontrol.samsung.adapter;

/**
 * Thrown when there is a problem in {@link TVAdapter}.
 *
 * @author Leszek
 */
public class TVAdapterException extends Exception {

	private static final long serialVersionUID = -737303600672861085L;

	/**
	 * Constructs a new exception.
	 *
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the cause
	 */
	public TVAdapterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception.
	 *
	 * @param message
	 *            the detail message
	 */
	public TVAdapterException(String message) {
		super(message);
	}
}
