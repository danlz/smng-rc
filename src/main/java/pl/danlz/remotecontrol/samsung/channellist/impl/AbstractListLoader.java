package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import pl.danlz.remotecontrol.samsung.channellist.Channel;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Base class for channel loaders.
 * <p>
 * Additional information:
 * <ul>
 * <li><a href="http://wiki.samygo.tv/index.php5/Samsung_channel_list_format">
 * http://wiki.samygo.tv/index.php5/Samsung_channel_list_format</a>
 * <li><a href="https://github.com/PredatH0r/ChanSort/wiki">https://github.com/
 * PredatH0r/ChanSort/wiki</a>
 * </ul>
 * </p>
 * <p>
 * See <a href=
 * "https://github.com/PredatH0r/ChanSort/blob/master/source/ChanSort.Loader.Samsung/ChanSort.Loader.Samsung.ini">
 * {@code ChanSort.Loader.Samsung.ini}</a> for byte offsets.
 * </p>
 * 
 * @author Leszek
 */
abstract class AbstractListLoader {

	private static final Logger LOG = Logger.getLogger(AbstractListLoader.class);

	private final ZipFile zip;
	private final String entryName;
	private final int recordLength;

	/**
	 * Constructs a new instance.
	 * 
	 * @param zip
	 *            the settings zip file
	 * @param entryName
	 *            zip entry name to load channels from
	 * @param recordLength
	 *            record length
	 */
	AbstractListLoader(ZipFile zip, String entryName, int recordLength) {
		this.zip = zip;
		this.entryName = entryName;
		this.recordLength = recordLength;
	}

	private List<Byte> getBytes(InputStream is) throws IOException {
		int nextByte = is.read();
		if (nextByte == -1) {
			// this means there are no more records in file
			return null;
		}
		List<Byte> record = new ArrayList<>(recordLength);
		record.add((byte) nextByte);
		byte[] buffer = new byte[recordLength - 1];
		while (record.size() < recordLength) {
			int readBytes = is.read(buffer);
			if (readBytes == -1 && record.size() < recordLength) {
				LOG.warn("Incomplete record in '" + entryName + "' entry");
				return null;
			}
			for (int i = 0; i < readBytes; i++) {
				record.add(buffer[i]);
			}
		}

		return record;
	}

	protected int getInt(List<Byte> bytes, int index) {
		return (bytes.get(index + 1) << 8) | (bytes.get(index) & 0xFF);
	}

	protected String getString(List<Byte> bytes, int index, int length) {
		byte[] fieldBytes = new byte[length];
		for (int i = 0; i < length; i++) {
			fieldBytes[i] = bytes.get(index + i);
		}

		return new String(fieldBytes, StandardCharsets.UTF_16).trim();
	}

	/**
	 * Parses given record.
	 * 
	 * @param record
	 *            record bytes
	 * @return a channel or {@code null} if the record does not represent a
	 *         valid channel
	 */
	protected abstract Channel parseRecord(List<Byte> record);

	/**
	 * Loads the channel list using specified parameters.
	 * 
	 * @return list of channels
	 * @throws IOException
	 *             if error occurs
	 */
	public List<Channel> load() throws IOException {
		List<Channel> channels = new ArrayList<>();

		ZipEntry entry = zip.getEntry(entryName);
		if (entry != null) {
			InputStream is = zip.getInputStream(entry);
			List<Byte> record = getBytes(is);
			while (record != null) {
				Channel channel = parseRecord(record);
				if (channel != null) {
					channels.add(channel);
				}
				record = getBytes(is);
			}
			LOG.info("Read " + channels.size() + " channels from '" + entryName + "'");
		} else {
			LOG.warn("Channel list '" + entryName + "' entry not found");
		}

		return channels;
	}
}
