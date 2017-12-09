package pl.danlz.remotecontrol.samsung.channellist;

import java.util.List;

import pl.danlz.remotecontrol.samsung.config.Configuration.ChannelListType;

/**
 * Provides information about channels.
 *
 * @author Leszek
 */
public interface ChannelListProvider {
	
	/**
	 * Selects given list type.
	 * 
	 * @param listType list type
	 */
	void selectChannelList(ChannelListType listType);

	/**
	 * Finds channels which names or channel numbers contain given text.
	 *
	 * @param text
	 *            text
	 * @return list of {@link Channel}
	 */
	List<Channel> find(String text);
}
