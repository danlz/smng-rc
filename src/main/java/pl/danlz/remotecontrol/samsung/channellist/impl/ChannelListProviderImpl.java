package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import pl.danlz.remotecontrol.samsung.channellist.Channel;
import pl.danlz.remotecontrol.samsung.channellist.ChannelListProvider;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Provides information about channels. This implementation reads the channel
 * data from TV settings file.
 * <p>
 * The following channel lists are supported:
 * <ul>
 * <li>map-CableD
 * <li>map-AirD
 * <li>map-AirA
 * </ul>
 * </p>
 *
 * @author Leszek
 */
public class ChannelListProviderImpl implements ChannelListProvider {

	private static final Logger LOG = Logger.getLogger(ChannelListProviderImpl.class);

	private static final String SETTINGS_FILE_EXTENSION = "scm";

	private List<Channel> channels = new ArrayList<>(0);

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
		if (files == null || files.length == 0) {
			LOG.info("No settings file found at [" + configDir + "]");
		} else {
			channels = readChannelListEntries(files[0]);
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

	private List<Channel> readChannelListEntries(File file) {
		LOG.info("Reading channel lists from file [" + file + "]");

		List<Channel> channels = new ArrayList<>();

		try (ZipFile zip = new ZipFile(file)) {
			AbstractListLoader loader = new CableDListLoader(zip);
			channels.addAll(loader.load());
			loader = new AirAListLoader(zip);
			channels.addAll(loader.load());
			loader = new AirDListLoader(zip);
			channels.addAll(loader.load());
		} catch (IOException e) {
			LOG.warn("Could not read channel lists from [" + file + "]", e);
		}

		return channels;
	}
}
