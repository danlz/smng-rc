package pl.danlz.remotecontrol.samsung.upnp.impl;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

/**
 * {@link UPnPAdapterImpl} tests.
 *
 * @author Leszek
 */
public class UPnPAdapterTest {

	private UPnPAdapterImpl testee = new UPnPAdapterImpl();

	@Test
	public void testParseResponse_Valid() throws Exception {
		String response = "HTTP/1.1 200 OK\r\n" + //
				"HEADER1:value1\r\n" + //
				"HEADER2:value2\r\n" + //
				"header3:VALUE3\r\n" + //
				"\r\n";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues,
				allOf( //
						hasEntry(equalTo("HEADER1"), equalTo("value1")), //
						hasEntry(equalTo("HEADER2"), equalTo("value2")), //
						hasEntry(equalTo("header3"), equalTo("VALUE3")) //
		));
	}

	@Test
	public void testParseResponse_ValidEmptyLines() throws Exception {
		String response = "HTTP/1.1 200 OK\r\n" + //
				"\r\n" + //
				"HEADER1:value1\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"HEADER2:value2\r\n" + //
				"\r\n";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues,
				allOf( //
						hasEntry(equalTo("HEADER1"), equalTo("value1")), //
						hasEntry(equalTo("HEADER2"), equalTo("value2")) //
		));
	}

	@Test
	public void testParseResponse_ValidWhitechars() throws Exception {
		String response = "HTTP/1.1 200 OK\r\n" + //
				"   HEADER  :  value   \r\n";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues, //
				hasEntry(equalTo("HEADER"), equalTo("value")) //
		);
	}

	@Test
	public void testParseResponse_HeaderWithoutValue() throws Exception {
		String response = "HTTP/1.1 200 OK\r\n" + //
				"HEADER:";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues, hasEntry( //
				equalTo("HEADER"), equalTo("") //
		));
	}

	@Test
	public void testParseResponse_ValueWithoutHeader() throws Exception {
		String response = "HTTP/1.1 200 OK\r\n" + //
				":value";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues, hasEntry( //
				equalTo(""), equalTo("value") //
		));
	}

	@Test
	public void testParseResponse_NoHeaderNoValue() throws Exception {
		String response = "HTTP/1.1 200 OK\r\n" + //
				":";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues, hasEntry( //
				equalTo(""), equalTo("") //
		));
	}

	@Test
	public void testParseResponse_StatusLineOnly() throws Exception {
		String response = "HTTP/1.1 200 OK";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues.entrySet(), emptyIterable());
	}

	@Test
	public void testParseResponse_InvalidStatus() throws Exception {
		String response = "HTTP/1.1 500 OK\r\n" + //
				"HEADER:value\r\n" + //
				"\r\n";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues.entrySet(), emptyIterable());
	}

	@Test
	public void testParseResponse_EmptyResponse() throws Exception {
		String response = "";

		Map<String, String> headerValues = testee.parseResponse(response);

		assertThat(headerValues.entrySet(), emptyIterable());
	}

	@Test(expected = NullPointerException.class)
	public void testParseResponse_NullResponse() throws Exception {
		String response = null;

		testee.parseResponse(response);
	}
}
