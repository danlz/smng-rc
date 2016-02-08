package pl.danlz.remotecontrol.samsung.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Configuration holder.
 *
 * @author Leszek
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "configuration")
public class Configuration {

	private static final Logger LOG = Logger.getLogger(Configuration.class);

	/**
	 * Path to directory where configuration files are stored.
	 */
	public static final String CONFIG_DIR_PATH = System.getProperty("user.home") + "/.smng-rc";

	private static final String CONFIG_FILE_PATH = CONFIG_DIR_PATH + "/config.xml";

	/**
	 * Default port.
	 */
	public static final int DEFAULT_PORT = 55000;

	/**
	 * Default controller name.
	 */
	public static final String DEFAULT_CONTROLLER_NAME = "SMNG-RC";

	/**
	 * Scan timeout.
	 */
	public static final int SCAN_TIMEOUT = 10;

	/**
	 * An amount of time to wait after sending a key to TV. This is needed,
	 * because TV needs some time to process the key.
	 */
	public static final int SEND_KEY_QUIET_PERIOD = 200;

	public enum ChannelSorting {
		NUMBER, NAME;
	}

	private static Configuration config;

	@XmlElement(name = "tv-address")
	private String tvAddress;

	@XmlElement(name = "tv-port")
	private int tvPort;

	@XmlElement(name = "controller-name")
	private String controllerName;

	@XmlElement(name = "scan-timeout")
	private int scanTimeout;

	@XmlElement(name = "channel-sorting")
	private ChannelSorting channelSorting;

	@XmlElement
	private Position position;

	@XmlElementWrapper(name = "regions")
	@XmlElement(name = "region")
	private List<Region> regions;

	private Configuration() {
		this(null, DEFAULT_PORT, DEFAULT_CONTROLLER_NAME, SCAN_TIMEOUT, ChannelSorting.NUMBER);
	}

	private Configuration(String tvAddress, int tvPort, String controllerName, int scanTimeout,
			ChannelSorting channelSorting) {
		this.tvAddress = tvAddress;
		this.tvPort = tvPort;
		this.controllerName = controllerName;
		this.scanTimeout = scanTimeout;
		this.channelSorting = channelSorting;
		this.regions = new ArrayList<>();
		regions.add(new Region("power"));
		regions.add(new Region("source"));
		regions.add(new Region("number"));
		regions.add(new Region("volProg"));
		regions.add(new Region("menuSmartGuide"));
		regions.add(new Region("arrows"));
		regions.add(new Region("abcd"));
		regions.add(new Region("shortcuts"));
		regions.add(new Region("control"));
	}

	/**
	 * Gets singleton instance of {@link Configuration}.
	 *
	 * @return {@link Configuration}
	 */
	public static synchronized Configuration getInstance() {
		if (config != null) {
			return config;
		}
		try {
			config = load();
			LOG.info("Loaded configuration from [" + CONFIG_FILE_PATH + "]");
		} catch (FileNotFoundException | JAXBException e) {
			LOG.info("Could not load configuration from [" + CONFIG_FILE_PATH + "]");
			config = new Configuration();
		}

		return config;
	}

	private static Configuration load() throws JAXBException, FileNotFoundException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		return (Configuration) unmarshaller.unmarshal(new FileInputStream(CONFIG_FILE_PATH));
	}

	/**
	 * Saves this configuration to file.
	 */
	public void save() {
		try {
			File file = new File(CONFIG_FILE_PATH);
			File directory = file.getParentFile();
			if (!directory.exists()) {
				directory.mkdir();
			}

			JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			marshaller.marshal(this, new FileOutputStream(CONFIG_FILE_PATH));
			LOG.info("Saved configuration to [" + CONFIG_FILE_PATH + "]");
		} catch (JAXBException | FileNotFoundException e) {
			LOG.error("Could not save configuration file", e);
		}
	}

	public String getTvAddress() {
		return tvAddress;
	}

	public void setTvAddress(String tvAddress) {
		this.tvAddress = tvAddress;
	}

	public int getTvPort() {
		return tvPort;
	}

	public void setTvPort(int tvPort) {
		this.tvPort = tvPort;
	}

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	public int getScanTimeout() {
		return scanTimeout;
	}

	public void setScanTimeout(int scanTimeout) {
		this.scanTimeout = scanTimeout;
	}

	public ChannelSorting getChannelSorting() {
		return channelSorting;
	}

	public void setChannelSorting(ChannelSorting channelSorting) {
		this.channelSorting = channelSorting;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public List<Region> getRegions() {
		return regions;
	}

	public void setRegions(List<Region> regions) {
		this.regions = regions;
	}

	@Override
	public String toString() {
		return "Configuration [tvAddress=" + tvAddress + ", tvPort=" + tvPort + ", controllerName=" + controllerName
				+ ", scanTimeout=" + scanTimeout + ", channelSorting=" + channelSorting + ", position=" + position
				+ ", regions=" + regions + "]";
	}

	/**
	 * Window position.
	 *
	 * @author Leszek
	 */
	public static class Position {

		private Integer x;
		private Integer y;

		public Position() {
		}

		public Position(Integer x, Integer y) {
			this.x = x;
			this.y = y;
		}

		public Integer getX() {
			return x;
		}

		public void setX(Integer x) {
			this.x = x;
		}

		public Integer getY() {
			return y;
		}

		public void setY(Integer y) {
			this.y = y;
		}

		@Override
		public String toString() {
			return "Position [x=" + x + ", y=" + y + "]";
		}
	}

	/**
	 * Region.
	 *
	 * @author Leszek
	 */
	public static class Region {

		@XmlAttribute
		private String name;

		public Region() {
		}

		public Region(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "Region [name=" + name + "]";
		}
	}
}
