package pl.danlz.remotecontrol.samsung.upnp;

import java.net.InetAddress;

/**
 * A UPnP device.
 *
 * @author Leszek
 */
public class UPnPDevice {

	private InetAddress address;
	private String st;
	private String usn;
	// properties below come from XML description
	private String deviceType;
	private String friendlyName;
	private String manufacturer;
	private String manufacturerURL;
	private String modelDescription;
	private String modelName;
	private String modelNumber;
	private String modelURL;
	private String serialNumber;
	private String udn;

	public UPnPDevice(InetAddress address, String st, String usn) {
		this(address, st, usn, null, null, null, null, null, null, null, null, null, null);
	}

	public UPnPDevice(InetAddress address, String st, String usn, String deviceType, String friendlyName,
			String manufacturer, String manufacturerURL, String modelDescription, String modelName, String modelNumber,
			String modelURL, String serialNumber, String udn) {
		this.address = address;
		this.st = st;
		this.usn = usn;
		this.deviceType = deviceType;
		this.friendlyName = friendlyName;
		this.manufacturer = manufacturer;
		this.manufacturerURL = manufacturerURL;
		this.modelDescription = modelDescription;
		this.modelName = modelName;
		this.modelNumber = modelNumber;
		this.modelURL = modelURL;
		this.serialNumber = serialNumber;
		this.udn = udn;
	}

	public InetAddress getAddress() {
		return address;
	}

	public String getSt() {
		return st;
	}

	public String getUsn() {
		return usn;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getManufacturerURL() {
		return manufacturerURL;
	}

	public String getModelDescription() {
		return modelDescription;
	}

	public String getModelName() {
		return modelName;
	}

	public String getModelNumber() {
		return modelNumber;
	}

	public String getModelURL() {
		return modelURL;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getUdn() {
		return udn;
	}

	@Override
	public String toString() {
		return "UPnPDevice [address=" + address + ", st=" + st + ", usn=" + usn + ", deviceType=" + deviceType
				+ ", friendlyName=" + friendlyName + ", manufacturer=" + manufacturer + ", manufacturerURL="
				+ manufacturerURL + ", modelDescription=" + modelDescription + ", modelName=" + modelName
				+ ", modelNumber=" + modelNumber + ", modelURL=" + modelURL + ", serialNumber=" + serialNumber
				+ ", udn=" + udn + "]";
	}
}
