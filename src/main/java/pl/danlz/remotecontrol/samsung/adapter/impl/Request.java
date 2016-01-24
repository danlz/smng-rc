package pl.danlz.remotecontrol.samsung.adapter.impl;

import java.util.Arrays;
import java.util.List;

/**
 * A request.
 *
 * @author Leszek
 */
class Request {

	/**
	 * Payload types.
	 */
	public enum PayloadType {

		AUTHENTICATE(new byte[] { 0x64, 0x00 }), //
		SEND_KEY(new byte[] { 0x00, 0x00, 0x00 });

		private byte[] typeHeader;

		private PayloadType(byte[] typeHeader) {
			this.typeHeader = typeHeader;
		}

		/**
		 * Gets type header bytes.
		 *
		 * @return bytes
		 */
		public byte[] getTypeHeader() {
			return typeHeader;
		}
	}

	/**
	 * A prefix - first byte in the request. Currently only {@code 0x00} is
	 * used.
	 */
	protected final byte prefix;

	/**
	 * A string. No particular meaning.
	 */
	protected final String aString;

	/**
	 * Payload type. Tells what action should be executed.
	 */
	protected final PayloadType payloadType;

	/**
	 * Messages. Additional data needed for given action.
	 */
	protected final List<String> messages;

	/**
	 * Creates a new instance.
	 *
	 * @param prefix
	 *            prefix
	 * @param aString
	 *            a string
	 * @param payloadType
	 *            payload type
	 * @param messages
	 *            messages
	 */
	public Request(int prefix, String aString, PayloadType payloadType, String... messages) {
		this.prefix = (byte) prefix;
		this.aString = aString;
		this.payloadType = payloadType;
		this.messages = Arrays.asList(messages);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param aString
	 *            a string
	 * @param payloadType
	 *            payload type
	 * @param messages
	 *            messages
	 */
	public Request(String aString, PayloadType payloadType, String... messages) {
		this(0x00, aString, payloadType, messages);
	}

	@Override
	public String toString() {
		return "Request [prefix=" + String.format("%02X", prefix) + ", aString=\"" + aString + "\", payloadType="
				+ payloadType + ", messages=" + messages + "]";
	}
}
