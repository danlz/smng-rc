package pl.danlz.remotecontrol.samsung.upnp;

import java.util.Collection;

/**
 * Adapter for communicating with UPnP devices on the network.
 *
 * @author Leszek
 */
public interface UPnPAdapter {

	/**
	 * Finds UPnP devices on the local network.
	 *
	 * @param searchTarget
	 *            search target string
	 * @param scanTimeout
	 *            scan timeout in seconds
	 * @return collection of UPnP devices
	 *
	 * @throws UPnPAdapterException
	 *             if a problem occurs during finding of devices
	 */
	Collection<UPnPDevice> findDevices(String searchTarget, int scanTimeout) throws UPnPAdapterException;
}
