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
		KEY_CONFIRMED2(new byte[] { 0x0A, 0x00, 0x18, 0x00, 0x00, 0x00 }), //
		KEY_CONFIRMED3(new byte[] { 0x0A, 0x00, 0x01, 0x00, 0x00, 0x00 }), //
		/**
		 * Observed only when in some menu or some app.
		 */
		KEY_CONFIRMED4(new byte[] { 0x0A, 0x00, 0x18, 0x00, 0x00, 0x00 }), //
		/**
		 * Observed only when in some menu, ie. menu, tools, channel list.
		 */
		WAITING_FOR_USER(new byte[] { 0x0A, 0x00, 0x02, 0x00, 0x00, 0x00 });

		private byte[] bytes;

		private Payload(byte[] bytes) {
			this.bytes = bytes;
		}

		/**
		 * Converts given bytes to specific constant.
		 *
		 * @param bytes
		 *            bytes
		 * @return constant
		 */
		public static Payload fromBytes(byte[] bytes) {
			for (Payload item : values()) {
				if (Arrays.equals(bytes, item.bytes)) {

					return item;
				}
			}
			throw new IllegalArgumentException("Unsupported payload - bytes: " + Arrays.toString(bytes));
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
