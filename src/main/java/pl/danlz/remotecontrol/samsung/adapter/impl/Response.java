package pl.danlz.remotecontrol.samsung.adapter.impl;

import java.util.Arrays;

/**
 * A response.
 *
 * @author Leszek
 */
class Response {

	/**
	 * Payloads.
	 */
	public enum Payload {

		ACCESS_GRANTED(new byte[] { 0x64, 0x00, 0x01, 0x00 }), //
		ACCESS_DENIED(new byte[] { 0x64, 0x00, 0x00, 0x00 }), //
		ABORT(new byte[] { 0x65, 0x00 }), //
		KEY_CONFIRMED(new byte[] { 0x00, 0x00, 0x00, 0x00 }), //
		/**
		 * Observed in menus, inside SmartHub applications or for macros
		 * (channel switch). Consecutive 4 bytes in the response contain
		 * additional information.
		 */
		KEY_CONFIRMED_EXT(new byte[] { 0x0A, 0x00 }), //
		/**
		 * Observed only when in some menu, ie. menu, tools, channel list.
		 */
		WAITING_FOR_USER(new byte[] { 0x0A, 0x00, 0x02, 0x00, 0x00, 0x00 });

		private byte[] prefix;

		private Payload(byte[] prefix) {
			this.prefix = prefix;
		}

		/**
		 * Converts given bytes to specific constant. The constants are matched
		 * by their prefixes.
		 *
		 * @param bytes
		 *            bytes
		 * @return constant
		 */
		public static Payload fromBytes(byte[] bytes) {
			for (Payload item : values()) {
				if (isPrefixMatched(item.prefix, bytes)) {

					return item;
				}
			}
			throw new IllegalArgumentException("Unsupported payload - bytes: " + Arrays.toString(bytes));
		}

		private static boolean isPrefixMatched(byte[] prefix, byte[] bytes) {
			if (prefix.length > bytes.length) {
				return false;
			}
			for (int i = 0; i < prefix.length; i++) {
				if (prefix[i] != bytes[i]) {
					return false;
				}
			}

			return true;
		}
	}

	/**
	 * A prefix - first byte of the response.
	 * <p>
	 * The observed values are: {@code 0x00} and {@code 0x02}.
	 * </p>
	 */
	protected byte prefix;

	/**
	 * A string.
	 * <p>
	 * Observed value: {@code iapp.samsung}.
	 * </p>
	 */
	protected String aString;

	/**
	 * Payload.
	 */
	protected Payload payload;

	@Override
	public String toString() {
		return "Response [prefix=" + String.format("%02X", prefix) + ", aString=\"" + aString + "\", payload=" + payload
				+ "]";
	}
}
