package pl.danlz.remotecontrol.samsung.logger;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple logger.
 *
 * @author Leszek
 */
public class Logger {

	private enum Level {
		ALL, DEBUG, INFO, WARN, ERROR, FATAL, OFF;
	}

	private static final Object MUTEX = new Object();

	private static final Level GLOBAL_THRESHOLD;
	private static final PrintStream TARGET_STREAM;
	private static final DateTimeFormatter DATE_TIME_FORMATTER;

	static {
		Level thresholdOverride = Level.INFO;
		String levelStr = System.getProperty("rcLogLevel");
		if (levelStr != null) {
			thresholdOverride = Level.valueOf(levelStr);
		}
		GLOBAL_THRESHOLD = thresholdOverride;

		String dateTimePatternOverride = "yyyy-MM-dd HH:mm:ss,SSS";
		String dateTimePatternStr = System.getProperty("rcLogDateTimePattern");
		if (dateTimePatternStr != null) {
			dateTimePatternOverride = dateTimePatternStr;
		}
		DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(dateTimePatternOverride);

		TARGET_STREAM = System.out;
	}

	private final Class<?> clazz;
	private final Level threshold;

	private Logger(Class<?> clazz, Level threshold) {
		this.clazz = clazz;
		this.threshold = threshold;
	}

	/**
	 * Gets a {@link Logger} for given class.
	 *
	 * @param clazz
	 *            class
	 * @return {@link Logger} instance
	 */
	public static Logger getLogger(Class<?> clazz) {
		return new Logger(clazz, GLOBAL_THRESHOLD);
	}

	void log(Level level, String message, Throwable throwable) {
		if (isLevelEnabled(level)) {
			synchronized (MUTEX) {
				String logEntry = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " " + String.format("%-5s", level)
						+ " [" + Thread.currentThread().getName() + "] " + clazz.getName() + " - " + message;

				TARGET_STREAM.println(logEntry);
				if (throwable != null) {
					throwable.printStackTrace(TARGET_STREAM);
				}
			}
		}
	}

	/**
	 * Logs given message at {@code DEBUG} level.
	 *
	 * @param message
	 *            message
	 */
	public void debug(String message) {
		log(Level.DEBUG, message, null);
	}

	/**
	 * Logs given message and stack trace of given throwable at {@code DEBUG}
	 * level.
	 *
	 * @param message
	 *            message
	 * @param throwable
	 *            throwable
	 */
	public void debug(String message, Throwable throwable) {
		log(Level.DEBUG, message, throwable);
	}

	/**
	 * Logs given message at {@code INFO} level.
	 *
	 * @param message
	 *            message
	 */
	public void info(String message) {
		log(Level.INFO, message, null);
	}

	/**
	 * Logs given message and stack trace of given throwable at {@code INFO}
	 * level.
	 *
	 * @param message
	 *            message
	 * @param throwable
	 *            throwable
	 */
	public void info(String message, Throwable throwable) {
		log(Level.INFO, message, throwable);
	}

	/**
	 * Logs given message at {@code WARN} level.
	 *
	 * @param message
	 *            message
	 */
	public void warn(String message) {
		log(Level.WARN, message, null);
	}

	/**
	 * Logs given message and stack trace of given throwable at {@code WARN}
	 * level.
	 *
	 * @param message
	 *            message
	 * @param throwable
	 *            throwable
	 */
	public void warn(String message, Throwable throwable) {
		log(Level.WARN, message, throwable);
	}

	/**
	 * Logs given message at {@code ERROR} level.
	 *
	 * @param message
	 *            message
	 */
	public void error(String message) {
		log(Level.ERROR, message, null);
	}

	/**
	 * Logs given message and stack trace of given throwable at {@code ERROR}
	 * level.
	 *
	 * @param message
	 *            message
	 * @param throwable
	 *            throwable
	 */
	public void error(String message, Throwable throwable) {
		log(Level.ERROR, message, throwable);
	}

	/**
	 * Logs given message at {@code FATAL} level.
	 *
	 * @param message
	 *            message
	 */
	public void fatal(String message) {
		log(Level.FATAL, message, null);
	}

	/**
	 * Logs given message and stack trace of given throwable at {@code FATAL}
	 * level.
	 *
	 * @param message
	 *            message
	 * @param throwable
	 *            throwable
	 */
	public void fatal(String message, Throwable throwable) {
		log(Level.FATAL, message, throwable);
	}

	boolean isLevelEnabled(Level level) {
		return level.ordinal() >= threshold.ordinal();
	}

	/**
	 * Checks if {@code DEBUG} or lower level is enabled.
	 *
	 * @return {@code true} if {@code DEBUG} or lower level is enabled;
	 *         otherwise {@code false}
	 */
	public boolean isDebugEnabled() {
		return isLevelEnabled(Level.DEBUG);
	}

	/**
	 * Checks if {@code INFO} or lower level is enabled.
	 *
	 * @return {@code true} if {@code INFO} or lower level is enabled; otherwise
	 *         {@code false}
	 */
	public boolean isInfoEnabled() {
		return isLevelEnabled(Level.INFO);
	}

	/**
	 * Checks if {@code WARN} or lower level is enabled.
	 *
	 * @return {@code true} if {@code WARN} or lower level is enabled; otherwise
	 *         {@code false}
	 */
	public boolean isWarnEnabled() {
		return isLevelEnabled(Level.WARN);
	}

	/**
	 * Checks if {@code ERROR} or lower level is enabled.
	 *
	 * @return {@code true} if {@code ERROR} or lower level is enabled;
	 *         otherwise {@code false}
	 */
	public boolean isErrorEnabled() {
		return isLevelEnabled(Level.ERROR);
	}

	/**
	 * Checks if {@code FATAL} or lower level is enabled.
	 *
	 * @return {@code true} if {@code FATAL} or lower level is enabled;
	 *         otherwise {@code false}
	 */
	public boolean isFatalEnabled() {
		return isLevelEnabled(Level.FATAL);
	}
}
