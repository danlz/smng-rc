package pl.danlz.remotecontrol.samsung.upnp.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import pl.danlz.remotecontrol.samsung.logger.Logger;
import pl.danlz.remotecontrol.samsung.upnp.UPnPAdapter;
import pl.danlz.remotecontrol.samsung.upnp.UPnPAdapterException;
import pl.danlz.remotecontrol.samsung.upnp.UPnPDevice;

/**
 * Adapter for communicating with UPnP devices on the network.
 *
 * @author Leszek
 */
public class UPnPAdapterImpl implements UPnPAdapter {
	private static final Logger LOG = Logger.getLogger(UPnPAdapterImpl.class);

	private final static Charset ENCODING = StandardCharsets.US_ASCII;
	private final static int RECEIVE_BUFFER_SIZE = 1024;
	private final static String HTTP_NEW_LINE = "\r\n";
	private final static String HEADER_VALUE_SEPARATOR = ":";
	private final static String QUERY_ADDRESS = "239.255.255.250";
	private final static int QUERY_PORT = 1900;
	private final static String MAN = "\"ssdp:discover\"";
	private final static int RESPONSE_TIMEOUT = 2;
	private final static String USER_AGENT = System.getProperty("os.name") + "/" + System.getProperty("os.version")
			+ " UPnP/1.1 SamsungRemoteControl/1.0";
	private final static String OK_STATUS_LINE = "HTTP/1.1 200 OK";

	private final Unmarshaller unmarshaller;

	/**
	 * Constructs a new instance.
	 */
	public UPnPAdapterImpl() {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(DeviceDescription.class);
			this.unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException("Could not initialize JAXB subsystem", e);
		}
	}

	@Override
	public Collection<UPnPDevice> findDevices(String searchTarget, int scanTimeout) throws UPnPAdapterException {
		String message = "M-SEARCH * HTTP/1.1" + HTTP_NEW_LINE + //
				"HOST: " + QUERY_ADDRESS + ":" + QUERY_PORT + HTTP_NEW_LINE + //
				"MAN: " + MAN + HTTP_NEW_LINE + //
				"MX: " + RESPONSE_TIMEOUT + HTTP_NEW_LINE + //
				"ST: " + searchTarget + HTTP_NEW_LINE + //
				"USER-AGENT: " + USER_AGENT + HTTP_NEW_LINE + //
				HTTP_NEW_LINE;

		try (DatagramSocket socket = new DatagramSocket()) {
			Collection<UPnPDevice> result = new ArrayList<>();

			InetAddress inetAddress = InetAddress.getByName(QUERY_ADDRESS);
			socket.setSoTimeout(RESPONSE_TIMEOUT * 1000);
			LOG.info("Socket bound to [" + socket.getLocalAddress() + ":" + socket.getLocalPort() + "]");

			LOG.info("Sending request... " + System.lineSeparator() + message);
			byte[] bytes = message.getBytes(ENCODING);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, inetAddress, QUERY_PORT);
			socket.send(packet);

			LocalDateTime endTime = LocalDateTime.now().plusSeconds(scanTimeout);
			while (LocalDateTime.now().isBefore(endTime)) {
				try {
					byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
					packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);

					String response = new String(packet.getData(), ENCODING);
					LOG.info("Received response from [" + packet.getAddress() + "]: " + System.lineSeparator()
							+ response);

					Map<String, String> headerValues = parseResponse(response);
					String location = headerValues.get("LOCATION");

					DeviceDescription description = getDeviceDescription(location);
					UPnPDevice device;
					if (description == null) {
						device = new UPnPDevice( //
								packet.getAddress(), //
								headerValues.get("ST"), //
								headerValues.get("USN") //
						);
					} else {
						device = new UPnPDevice( //
								packet.getAddress(), //
								headerValues.get("ST"), //
								headerValues.get("USN"), //
								description.getDevice().getDeviceType(), //
								description.getDevice().getFriendlyName(), //
								description.getDevice().getManufacturer(), //
								description.getDevice().getManufacturerURL(), //
								description.getDevice().getModelDescription(), //
								description.getDevice().getModelName(), //
								description.getDevice().getModelNumber(), //
								description.getDevice().getModelURL(), //
								description.getDevice().getSerialNumber(), //
								description.getDevice().getUdn() //
						);
					}
					result.add(device);
				} catch (SocketTimeoutException e) {
					LOG.debug("Search response timeout");
				}
			}

			return result;
		} catch (IOException e) {
			throw new UPnPAdapterException("Problem occured during finding of devices", e);
		}
	}

	DeviceDescription getDeviceDescription(String location) {
		try {
			URL url = new URL(location);
			InputStream inputStream = url.openStream();

			return (DeviceDescription) unmarshaller.unmarshal(inputStream);
		} catch (IOException | JAXBException e) {
			LOG.info("Could not get device description from: " + location, e);
		}

		return null;
	}

	Map<String, String> parseResponse(String response) throws UPnPAdapterException {
		if (response == null) {
			throw new NullPointerException("response may not be null");
		}
		Map<String, String> result = new HashMap<>();

		String[] lines = response.split(HTTP_NEW_LINE.replaceAll("\\\\", "\\\\"));
		if (lines.length == 0) {
			// throw new UPnPAdapterException("Empty response received");
			return result;
		}
		String statusLine = lines[0];
		if (!OK_STATUS_LINE.equals(statusLine)) {
			// throw new UPnPAdapterException("Invalid status received [" +
			// statusLine + "]");
			return result;
		}
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];
			if ("".equals(line.trim())) {
				continue;
			}
			int separatorIndex = line.indexOf(HEADER_VALUE_SEPARATOR);
			if (separatorIndex == -1) {
				continue;
			}
			String header = line.substring(0, separatorIndex).trim();
			String value = line.substring(separatorIndex + 1).trim();
			result.put(header, value);
		}

		return result;
	}

}
