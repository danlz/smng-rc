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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
	/**
	 * Response delay in seconds. Should be between 1 and 5.
	 */
	private final static int RESPONSE_DELAY = 1;
	private final static String USER_AGENT = System.getProperty("os.name") + "/" + System.getProperty("os.version")
			+ " UPnP/1.1 SMNG-RC/1.0";
	private final static String OK_STATUS_LINE = "HTTP/1.1 200 OK";
	private static final String LOCATION_HEADER_NAME = "LOCATION";
	private static final String UNIQUE_SERVICE_NAME_HEADER_NAME = "USN";
	private static final String SEARCH_TARGET_HEADER_NAME = "ST";

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
				"MX: " + RESPONSE_DELAY + HTTP_NEW_LINE + //
				"ST: " + searchTarget + HTTP_NEW_LINE + //
				"USER-AGENT: " + USER_AGENT + HTTP_NEW_LINE + //
				HTTP_NEW_LINE;

		try (DatagramSocket socket = new DatagramSocket()) {
			Map<UPnPDevice, Map<String, String>> deviceToHeadersMap = new HashMap<>();

			InetAddress inetAddress = InetAddress.getByName(QUERY_ADDRESS);
			// 500ms additionally to make sure we receive the packet
			socket.setSoTimeout(RESPONSE_DELAY * 1000 + 500);
			LOG.info("Socket bound to [" + socket.getLocalAddress() + ":" + socket.getLocalPort() + "]");

			LOG.info("Sending request... ");
			LOG.debug(System.lineSeparator() + message);
			byte[] bytes = message.getBytes(ENCODING);
			DatagramPacket packetToSend = new DatagramPacket(bytes, bytes.length, inetAddress, QUERY_PORT);
			socket.send(packetToSend);

			LocalDateTime endTime = LocalDateTime.now().plusSeconds(scanTimeout);
			while (LocalDateTime.now().isBefore(endTime)) {
				try {
					byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
					DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

					LOG.debug("Waiting for response...");
					socket.receive(receivedPacket);

					byte[] receivedBytes = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());
					String response = new String(receivedBytes, ENCODING);
					LOG.info("Received response from [" + receivedPacket.getAddress() + "]");
					LOG.debug(System.lineSeparator() + response);

					Map<String, String> headerValues = parseResponse(response);

					UPnPDevice device = new UPnPDevice( //
							receivedPacket.getAddress(), //
							headerValues.get(SEARCH_TARGET_HEADER_NAME), //
							headerValues.get(UNIQUE_SERVICE_NAME_HEADER_NAME) //
					);

					deviceToHeadersMap.put(device, headerValues);
				} catch (SocketTimeoutException e) {
					LOG.debug("Search response timeout");

					LOG.info("Resending request...");
					socket.send(packetToSend);
				}
			}
			LOG.info("Search finished");

			Collection<UPnPDevice> result = new HashSet<>();
			for (Map.Entry<UPnPDevice, Map<String, String>> entry : deviceToHeadersMap.entrySet()) {
				Map<String, String> headerValues = entry.getValue();
				String location = headerValues.get(LOCATION_HEADER_NAME);

				LOG.info("Getting description for " + entry.getKey());
				DeviceDescription description = getDeviceDescription(location);
				if (description == null) {
					result.add(entry.getKey());
				} else {
					result.add(new UPnPDevice( //
							entry.getKey().getAddress(), //
							entry.getKey().getSt(), //
							entry.getKey().getUsn(), //
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
					));
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
			LOG.error("Could not get device description from: " + location, e);
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
			String header = line.substring(0, separatorIndex).trim().toUpperCase();
			String value = line.substring(separatorIndex + 1).trim();
			result.put(header, value);
		}

		return result;
	}
}
