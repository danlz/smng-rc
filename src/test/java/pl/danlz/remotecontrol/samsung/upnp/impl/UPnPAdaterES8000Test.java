package pl.danlz.remotecontrol.samsung.upnp.impl;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.util.Collection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import pl.danlz.remotecontrol.samsung.upnp.UPnPDevice;

/**
 * {@link UPnPAdapterImpl} tests with a real ES8000 TV.
 *
 * @author Leszek
 */
@Ignore
public class UPnPAdaterES8000Test {

	private final static int SCAN_TIMEOUT = 0;

	private UPnPAdapterImpl testee = new UPnPAdapterImpl();

	@BeforeClass
	public static void setUp() {
		System.setProperty("rcLogLevel", "DEBUG");
	}

	@Test
	public void testFindDevices_RealNetwork() throws Exception {
		String samsungRemoteControllerReceiverTarget = "urn:samsung.com:device:RemoteControlReceiver:1";
		// "ssdp:all" finds all devices

		Collection<UPnPDevice> devices = testee.findDevices(samsungRemoteControllerReceiverTarget, SCAN_TIMEOUT);

		System.out.println("Devices: " + devices);

		Assert.assertThat(devices,
				contains(allOf( //
						hasProperty("st", equalTo(samsungRemoteControllerReceiverTarget)), //
						hasProperty("manufacturer", equalTo("Samsung Electronics")), //
						hasProperty("manufacturerURL", equalTo("http://www.samsung.com/sec")), //
						hasProperty("modelDescription", equalTo("Samsung TV RCR")), //
						hasProperty("modelName", equalTo("UE46ES8000")) //
		)));
	}

}
