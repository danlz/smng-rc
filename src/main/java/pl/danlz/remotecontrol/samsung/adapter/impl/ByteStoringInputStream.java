package pl.danlz.remotecontrol.samsung.adapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A decorator around an {@link InputStream} that stores all the read bytes.
 * 
 * @author Leszek
 */
class ByteStoringInputStream extends InputStream {

	private List<Byte> storedBytes = new ArrayList<>();
	private InputStream wrappedStream;

	/**
	 * Constructs a new instance.
	 * 
	 * @param wrappedStream
	 *            an {@link InputStream} to read from
	 */
	public ByteStoringInputStream(InputStream wrappedStream) {
		this.wrappedStream = wrappedStream;
	}

	@Override
	public int read() throws IOException {
		int aByte = wrappedStream.read();
		if (aByte != -1) {
			storedBytes.add(Byte.valueOf((byte) aByte));
		}
		return aByte;
	}

	/**
	 * Get bytes currently stored bytes.
	 * 
	 * @return stored bytes
	 */
	public byte[] getBytes() {
		byte[] bytes = new byte[storedBytes.size()];
		for (int i = 0; i < storedBytes.size(); i++) {
			bytes[i] = storedBytes.get(i);
		}

		return bytes;
	}
}
