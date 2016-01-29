package pl.danlz.remotecontrol.samsung.upnp.impl;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;

import java.util.Collection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.upnp.UPnPDevice;

/**
 * {@link UPnPAdapterImpl} tests on a real network with a UE46ES8000 TV.
 *
 * @author Leszek
 */
public class UPnPAdaterRealNetworkTest {

	private final static int SCAN_TIMEOUT = Configuration.SCAN_TIMEOUT;

	private UPnPAdapterImpl testee = new UPnPAdapterImpl();

	@BeforeClass
	public static void setUp() {
		System.setProperty("rcLogLevel", "DEBUG");
	}

	@Ignore
	@Test
	public void testFindDevices_UE46ES8000() throws Exception {
		String samsungRemoteControllerReceiverTarget = "urn:samsung.com:device:RemoteControlReceiver:1";

		Collection<UPnPDevice> devices = testee.findDevices(samsungRemoteControllerReceiverTarget, SCAN_TIMEOUT);

		printDevices(devices);

		Assert.assertThat(devices,
				contains(allOf( //
						hasProperty("st", equalTo(samsungRemoteControllerReceiverTarget)), //
						hasProperty("manufacturer", equalTo("Samsung Electronics")), //
						hasProperty("manufacturerURL", equalTo("http://www.samsung.com/sec")), //
						hasProperty("modelDescription", equalTo("Samsung TV RCR")), //
						hasProperty("modelName", equalTo("UE46ES8000")) //
		)));
	}

	@Test
	public void testFindDevices_ssdp_all() throws Exception {
		String searchTarget = "ssdp:all";

		Collection<UPnPDevice> devices = testee.findDevices(searchTarget, SCAN_TIMEOUT);

		printDevices(devices);

		// Windows itself answers with its services
		Assert.assertThat(devices, not(empty()));
	}

	private void printDevices(Collection<UPnPDevice> devices) {
		System.out.println("Devices:");
		for (UPnPDevice device : devices) {
			System.out.println(device.toString());
		}
	}
}
