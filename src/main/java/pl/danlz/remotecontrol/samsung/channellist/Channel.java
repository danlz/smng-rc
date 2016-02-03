package pl.danlz.remotecontrol.samsung.channellist;

/**
 * Channel.
 *
 * @author Leszek
 */
public class Channel {

	/**
	 * Service type.
	 */
	public enum ServiceType {

		EMPTY(0x00), SD_TV(0x01), RADIO(0x02), TELETEXT(0x03), FM_RADIO(0x07), AAC_RADIO(0x0A), DATA(0x0C), HD_TV2(
				0x11), SD_TV2(0x16), HD_TV(0x19), UHD_TV(0x1F), OPTION(0xDD);

		private int type;

		private ServiceType(int type) {
			this.type = type;
		}

		/**
		 * Creates {@link ServiceType} from given type.
		 *
		 * @param type
		 *            type
		 * @return {@link ServiceType}
		 * @throws IllegalArgumentException
		 *             if given type is unknown
		 */
		public static ServiceType fromType(int type) {
			for (ServiceType st : values()) {
				if (type == st.type) {
					return st;
				}
			}
			throw new IllegalArgumentException("Unknown service type: " + type);
		}
	}

	private int number;

	private int logicalNumber;

	private String name;

	private ServiceType serviceType;

	/**
	 * Creates a new instance.
	 *
	 * @param number
	 *            channel number
	 * @param name
	 *            channel name
	 */
	public Channel(int number, int logicalNumber, String name, ServiceType serviceType) {
		this.number = number;
		this.logicalNumber = logicalNumber;
		this.name = name;
		this.serviceType = serviceType;
	}

	public int getNumber() {
		return number;
	}

	public int getLogicalNumber() {
		return logicalNumber;
	}

	public String getName() {
		return name;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	@Override
	public String toString() {
		return "Channel [number=" + number + ", logicalNumber=" + logicalNumber + ", name=" + name + ", serviceType="
				+ serviceType + "]";
	}
}