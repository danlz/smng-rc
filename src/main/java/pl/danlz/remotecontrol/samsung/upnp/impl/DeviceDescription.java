package pl.danlz.remotecontrol.samsung.upnp.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Device description from XML.
 *
 * @author Leszek
 */
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
class DeviceDescription {

	@XmlElement
	private Device device;

	public Device getDevice() {
		return device;
	}
}
