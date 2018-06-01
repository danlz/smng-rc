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
import pl.danlz.remotecontrol.samsung.config.Configuration.ChannelListType;
import pl.danlz.remotecontrol.samsung.context.AppCtx;
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
 * <li>map-SateD
 * </ul>
 * </p>
 *
 * @author Leszek
 */
public class ChannelListProviderImpl implements ChannelListProvider {

	private static final Logger LOG = Logger.getLogger(ChannelListProviderImpl.class);

	private static final String SETTINGS_FILE_EXTENSION = "scm";

	private final Configuration config = AppCtx.getBean(Configuration.class);

	private List<Channel> cableDChannels = new ArrayList<>(0);
	private List<Channel> airDChannels = new ArrayList<>(0);
	private List<Channel> airAChannels = new ArrayList<>(0);
	private List<Channel> satelliteDChannels = new ArrayList<>(0);

	private List<Channel> currentChannelList = new ArrayList<>(0);

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
			readChannelListEntries(files[0]);
			if (config.getChannelListType() != null) {
				selectChannelList(config.getChannelListType());
			}
		}
	}

	@Override
	public void selectChannelList(ChannelListType listType) {
		switch (listType) {
		case CABLE_D:
			currentChannelList = cableDChannels;
			break;
		case AIR_D:
			currentChannelList = airDChannels;
			break;
		case AIR_A:
			currentChannelList = airAChannels;
			break;
		case SATELLITE_D:
			currentChannelList = satelliteDChannels;
			break;
		default:
			throw new IllegalArgumentException("Unknown channel list type: " + listType);
		}
		LOG.debug("'" + listType + "' list selected");
	}

	@Override
	public List<Channel> find(String text) {
		if (text == null || text.isEmpty()) {
			return new ArrayList<Channel>(currentChannelList);
		}
		return currentChannelList.stream().filter(p -> //
		p.getName() != null && p.getName().toLowerCase().contains(text) || //
				String.valueOf(p.getNumber()).contains(text) //
		).collect(Collectors.toList());
	}

	private void readChannelListEntries(File file) {
		LOG.info("Reading channel lists from file [" + file + "]");

		try (ZipFile zip = new ZipFile(file)) {
			cableDChannels.addAll(new CableDListLoader(zip).load());
			logChannels("CableD", cableDChannels);
			airDChannels.addAll(new AirDListLoader(zip).load());
			logChannels("AirD", airDChannels);
			airAChannels.addAll(new AirAListLoader(zip).load());
			logChannels("AirA", airAChannels);
			satelliteDChannels.addAll(new SateDListLoader(zip).load());
			logChannels("SateD", satelliteDChannels);
		} catch (IOException e) {
			LOG.warn("Could not read channel lists from [" + file + "]", e);
		}
	}

	private void logChannels(String listName, List<Channel> channels) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(listName + " channels:");
			for (Channel ch : channels) {
				LOG.debug(ch.toString());
			}
		}
	}
}
