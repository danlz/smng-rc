package pl.danlz.remotecontrol.samsung.adapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapterException;
import pl.danlz.remotecontrol.samsung.adapter.impl.Request.PayloadType;
import pl.danlz.remotecontrol.samsung.adapter.impl.Response.Payload;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Adapter for communicating with the TV.
 * <p>
 * Based on <a href=
 * "http://sc0ty.pl/2012/02/samsung-tv-network-remote-control-protocol>http://sc0ty.pl/2012/02/samsung-tv-network-remote-control-protocol</a>
 * .</p>
 *
 * @author Leszek
 */
public class TVAdapterImpl implements TVAdapter {

	private static final Logger LOG = Logger.getLogger(TVAdapterImpl.class);

	private static final Charset CHARSET = StandardCharsets.US_ASCII;

	private static final int CONNECT_TIMEOUT = 2000;
	private static final int READ_TIMEOUT = 2000;

	/**
	 * Some string. Exact meaning is not known.
	 */
	private static final String A_STRING = "samsung.remotecontrol.danlz.pl";

	private Socket socket;

	@Override
	public void connect(String address, int port, String controllerName) throws TVAdapterException {
		connect(address, port);
		authenticate(controllerName);
	}

	private void connect(String address, int port) throws TVAdapterException {
		try {
			socket = new Socket();
			socket.setSoTimeout(READ_TIMEOUT);
			socket.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT);
			LOG.info("Connected to " + socket.getRemoteSocketAddress());
		} catch (IOException e) {
			throw new TVAdapterException("Could not connect to TV at \"" + address + ":" + port + "\"", e);
		}
	}

	private void authenticate(String controllerName) throws TVAdapterException {
		try {
			LOG.info("Authenticating...");
			InetAddress clientAddress = socket.getLocalAddress();
			String clientMac = formatMAC(NetworkInterface.getByInetAddress(clientAddress).getHardwareAddress());
			Request request = new Request(A_STRING, PayloadType.AUTHENTICATE, //
					clientAddress.getHostAddress(), clientMac, controllerName);
			Response response = sendRequest(request);
			if (response.payload != Payload.ACCESS_GRANTED && //
					response.payload != Payload.WAITING_FOR_USER) {
				throw new TVAdapterException("authenticate: Access denied: " + response);
			}
		} catch (IOException e) {
			throw new TVAdapterException("Could not authenticate", e);
		}
	}

	@Override
	public void sendKey(String keyCode) throws TVAdapterException {
		try {
			LOG.info("Sending key \"" + keyCode + "\"...");
			Request request = new Request(A_STRING, PayloadType.SEND_KEY, keyCode);
			Response response = sendRequest(request);
			if (response.payload != Payload.KEY_CONFIRMED && //
					response.payload != Payload.KEY_CONFIRMED2 && //
					response.payload != Payload.KEY_CONFIRMED3 && //
					response.payload != Payload.KEY_CONFIRMED4 && //
					response.payload != Payload.KEY_CONFIRMED5 && //
					response.payload != Payload.WAITING_FOR_USER) {
				throw new TVAdapterException("sendKey: not confirmed: " + response);
			}
		} catch (IOException e) {
			throw new TVAdapterException("Could not send key \"" + keyCode + "\"", e);
		}
	}

	@Override
	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown()
				&& !socket.isClosed();
	}

	@Override
	public void close() {
		try {
			if (socket != null) {
				socket.close();
				LOG.info("Connection closed");
			}
		} catch (IOException e) {
			LOG.warn("Could not close connection to TV", e);
		}
	}

	private String formatMAC(byte[] macBytes) {
		String mac = "";
		for (byte b : macBytes) {
			mac += String.format("%02X:", b);
		}
		if (!mac.isEmpty()) {
			mac = mac.substring(0, mac.length() - 1);
		}

		return mac;
	}

	private Response sendRequest(Request request) throws IOException, TVAdapterException {
		LOG.debug("Sending " + request);
		OutputStream out = socket.getOutputStream();
		byte[] bytes = serializeRequest(request);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sending bytes: " + bytesToString(bytes));
		}
		out.write(bytes);
		out.flush();

		return receiveResponse();
	}

	private Response receiveResponse() throws IOException, TVAdapterException {
		InputStream in = socket.getInputStream();
		byte[] prefix = readBytes(in, 1);
		byte[] aString = readField(in);
		byte[] payload = readField(in);

		if (LOG.isDebugEnabled()) {
			byte[] result = prefix;
			result = appendBytes(result, aString);
			result = appendBytes(result, payload);
			LOG.debug("Received bytes: " + bytesToString(result));
		}

		Response response = new Response();
		response.prefix = prefix[0];
		response.aString = new String(aString, CHARSET);
		response.payload = Payload.fromBytes(payload);

		LOG.debug("Received " + response);

		return response;
	}

	private byte[] readField(InputStream in) throws IOException, TVAdapterException {
		byte[] lengthBytes = readBytes(in, 2);
		int length = deserializeLength(lengthBytes);
		byte[] valueBytes = readBytes(in, length);

		return valueBytes;
	}

	private byte[] readBytes(InputStream in, int bytesToRead) throws IOException, TVAdapterException {
		byte[] result = new byte[] {};
		int bytesRead = 0;
		int byteRead = -2;

		while (byteRead != -1 && bytesRead < bytesToRead) {
			byteRead = in.read();
			bytesRead++;
			if (LOG.isDebugEnabled()) {
				LOG.debug("Byte read: " + String.format("%02X", byteRead) + " [" + byteRead + "]");
			}
			result = appendBytes(result, new byte[] { (byte) byteRead });
		}
		if (byteRead == -1) {
			throw new TVAdapterException("Stream closed - bytes read: " + bytesRead);
		}

		return result;
	}

	private byte[] serializeLength(int length) {
		// little endian
		return new byte[] { (byte) (length & 0x00FF), (byte) (length >> 8) };
	}

	private int deserializeLength(byte[] bytes) {
		// little endian
		return ((bytes[1] << 8) | bytes[0]);
	}

	private byte[] serializeRequest(Request request) throws UnsupportedEncodingException, UnknownHostException {
		byte[] bytes = new byte[] { request.prefix };
		bytes = appendField(bytes, request.aString.getBytes(CHARSET));

		byte[] payloadBytes = request.payloadType.getTypeHeader();
		for (String message : request.messages) {
			payloadBytes = appendField(payloadBytes, Base64.encode(message.getBytes(CHARSET)).getBytes(CHARSET));
		}
		bytes = appendField(bytes, payloadBytes);

		return bytes;
	}

	private byte[] appendBytes(byte[] src, byte[] suffix) {
		int newLength = src.length + suffix.length;
		byte[] newArray = Arrays.copyOf(src, newLength);
		for (int i = 0; i < suffix.length; i++) {
			newArray[src.length + i] = suffix[i];
		}

		return newArray;
	}

	private byte[] appendField(byte[] src, byte[] fieldBytes) throws UnsupportedEncodingException {
		byte[] lengthBytes = serializeLength(fieldBytes.length);
		byte[] result = appendBytes(src, lengthBytes);
		result = appendBytes(result, fieldBytes);

		return result;
	}

	private String bytesToString(byte[] bytes) {
		int i = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder sbText = new StringBuilder();
		sb.append("\n");
		for (byte b : bytes) {
			if (b < 32 || b > 127) {
				sbText.append(".");
			} else {
				sbText.append((char) b);
			}

			sb.append(String.format("%02X", b));
			sb.append(" ");
			i++;
			if (i % 8 == 0) {
				sb.append(" ");
				sbText.append(" ");
			}
			if (i % 16 == 0) {
				sb.append("\n");
				sbText.append("\n");
			}
		}

		return sb.toString() + "\n------------------------------------------------\n" + sbText.toString();
	}

}
