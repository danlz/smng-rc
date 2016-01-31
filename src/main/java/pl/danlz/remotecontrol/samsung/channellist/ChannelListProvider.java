package pl.danlz.remotecontrol.samsung.channellist;

import java.util.List;

/**
 * Provides information about channels.
 *
 * @author Leszek
 */
public interface ChannelListProvider {

	/**
	 * Finds channels which names or channel numbers contain given text.
	 *
	 * @param text
	 *            text
	 * @return list of {@link Channel}
	 */
	List<Channel> find(String text);
}
