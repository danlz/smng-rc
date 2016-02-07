package pl.danlz.remotecontrol.samsung.adapter;

/**
 * Thrown when confirmation is needed during authentication.
 * 
 * @author Leszek
 */
public class TVAuthenticationException extends TVAdapterException {

	private static final long serialVersionUID = -5263921852994240876L;

	/**
	 * Constructs a new exception.
	 *
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the cause
	 */
	public TVAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception.
	 *
	 * @param message
	 *            the detail message
	 */
	public TVAuthenticationException(String message) {
		super(message);
	}

}
