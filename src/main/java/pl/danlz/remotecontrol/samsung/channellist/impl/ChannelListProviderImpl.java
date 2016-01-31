package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import pl.danlz.remotecontrol.samsung.channellist.Channel;
import pl.danlz.remotecontrol.samsung.channellist.Channel.ServiceType;
import pl.danlz.remotecontrol.samsung.channellist.ChannelListProvider;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Provides information about channels. This implementation reads channel data
 * from TV settings file.
 * <p>
 * Currently only map-AirD/map-CableD channels are supported. Useful links:
 * <ul>
 * <li><a href="http://wiki.samygo.tv/index.php5/Samsung_channel_list_format">
 * http://wiki.samygo.tv/index.php5/Samsung_channel_list_format</a>
 * <li><a href="https://github.com/PredatH0r/ChanSort/wiki">https://github.com/
 * PredatH0r/ChanSort/wiki</a>
 * </ul>
 * </p>
 *
 * @author Leszek
 */
public class ChannelListProviderImpl implements ChannelListProvider {

	private static final Logger LOG = Logger.getLogger(ChannelListProviderImpl.class);

	private static final String ZIP_ENTRY_PATH = "map-CableD";
	private static final String SETTINGS_FILE_EXTENSION = "scm";
	private static final int RECORD_LENGTH = 320; // E series

	private List<Channel> channels;

	// TODO code cleanup, constants, variables, etc.

	/**
	 * Creates a new instance.
	 */
	public ChannelListProviderImpl() {
		File configDir = new File(Configuration.CONFIG_DIR_PATH);
		File[] files = configDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith("." + SETTINGS_FILE_EXTENSION);
			}
		});
		if (files.length == 0) {
			LOG.info("No settings file found at [" + configDir + "]");
		} else {
			channels = readChannelList(files[0]);
			// TODO configurable sorting: names or channel numbers
			channels.sort(new Comparator<Channel>() {

				@Override
				public int compare(Channel o1, Channel o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
			LOG.debug("Channels:");
			for (Channel ch : channels) {
				LOG.debug(ch.toString());
			}
		}
	}

	@Override
	public List<Channel> find(String text) {
		if (text == null || text.isEmpty()) {
			return new ArrayList<Channel>(channels);
		}
		return channels.stream().filter(p -> //
		p.getName() != null && p.getName().toLowerCase().contains(text) || //
				String.valueOf(p.getNumber()).contains(text) //
		).collect(Collectors.toList());
	}

	private List<Channel> readChannelList(File file) {
		LOG.info("Reading channel list from file [" + file + "]");

		List<Channel> channels = new ArrayList<>();

		try (ZipFile zip = new ZipFile(file)) {
			ZipEntry entry = zip.getEntry(ZIP_ENTRY_PATH);
			if (entry != null) {
				InputStream is = zip.getInputStream(entry);
				List<Byte> record = getBytes(is);
				while (record != null) {
					Channel channel = parseRecord(record);
					if (channel.getNumber() > 0) {
						channels.add(channel);
					}
					record = getBytes(is);
				}
				LOG.info("Read " + channels.size() + " channels");
			} else {
				LOG.warn("'" + ZIP_ENTRY_PATH + "' entry not found in [" + file + "]");
			}
		} catch (IOException e) {
			LOG.warn("Could not read channels from [" + file + "]", e);
		}

		return channels;
	}

	private List<Byte> getBytes(InputStream is) throws IOException {
		int nextByte = is.read();
		if (nextByte == -1) {
			// this means there are no more records in file
			return null;
		}
		List<Byte> record = new ArrayList<>(RECORD_LENGTH);
		record.add((byte) nextByte);
		byte[] buffer = new byte[RECORD_LENGTH - 1];
		while (record.size() < RECORD_LENGTH) {
			int readBytes = is.read(buffer);
			if (readBytes == -1 && record.size() < RECORD_LENGTH) {
				LOG.warn("Incomplete record in '" + ZIP_ENTRY_PATH + "' entry");
				return null;
			}
			for (int i = 0; i < readBytes; i++) {
				record.add(buffer[i]);
			}
		}

		return record;
	}

	private Channel parseRecord(List<Byte> record) {
		int number = getInt(record, 0);
		int logicalNumber = getInt(record, 44);
		String name = getString(record, 64, 100);
		ServiceType serviceType = ServiceType.fromType(record.get(15));

		return new Channel(number, logicalNumber, name, serviceType);
	}

	private int getInt(List<Byte> bytes, int index) {
		return (bytes.get(index + 1) << 8) | (bytes.get(index) & 0xFF);
	}

	private String getString(List<Byte> bytes, int index, int length) {
		byte[] fieldBytes = new byte[length];
		for (int i = 0; i < length; i++) {
			fieldBytes[i] = bytes.get(index + i);
		}

		return new String(fieldBytes, StandardCharsets.UTF_16).trim();
	}
}
