package pl.danlz.remotecontrol.samsung.adapter.impl;

/**
 * Base64 encoder.
 * <p>
 * Implementation from:
 * <a href="https://en.wikipedia.org/wiki/Base64">https://en.wikipedia.org/wiki/
 * Base64</a>
 * <p>
 *
 * @author Leszek
 */
class Base64 {

	private static final String CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	/**
	 * Encodes given bytes with Base64 encoding.
	 *
	 * @param in
	 *            input bytes
	 * @return Base64 string
	 */
	static String encode(byte[] in) {
		StringBuilder out = new StringBuilder((in.length * 4) / 3);
		int b;
		for (int i = 0; i < in.length; i += 3) {
			b = (in[i] & 0xFC) >> 2;
			out.append(CODES.charAt(b));
			b = (in[i] & 0x03) << 4;
			if (i + 1 < in.length) {
				b |= (in[i + 1] & 0xF0) >> 4;
				out.append(CODES.charAt(b));
				b = (in[i + 1] & 0x0F) << 2;
				if (i + 2 < in.length) {
					b |= (in[i + 2] & 0xC0) >> 6;
					out.append(CODES.charAt(b));
					b = in[i + 2] & 0x3F;
					out.append(CODES.charAt(b));
				} else {
					out.append(CODES.charAt(b));
					out.append('=');
				}
			} else {
				out.append(CODES.charAt(b));
				out.append("==");
			}
		}

		return out.toString();
	}
}
