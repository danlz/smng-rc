package pl.danlz.remotecontrol.samsung.upnp.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Device from XML.
 *
 * @author Leszek
 */
@XmlAccessorType(XmlAccessType.FIELD)
class Device {

	@XmlElement
	private String deviceType;

	@XmlElement
	private String friendlyName;

	@XmlElement
	private String manufacturer;

	@XmlElement
	private String manufacturerURL;

	@XmlElement
	private String modelDescription;

	@XmlElement
	private String modelName;

	@XmlElement
	private String modelNumber;

	@XmlElement
	private String modelURL;

	@XmlElement
	private String serialNumber;

	@XmlElement(name = "UDN")
	private String udn;

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

}
