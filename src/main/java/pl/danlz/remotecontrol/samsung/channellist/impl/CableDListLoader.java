package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.util.List;
import java.util.zip.ZipFile;

import pl.danlz.remotecontrol.samsung.channellist.Channel;
import pl.danlz.remotecontrol.samsung.channellist.Channel.ServiceType;

/**
 * Loader for {@code map-CableD} list.
 * 
 * @author Leszek
 */
class CableDListLoader extends AbstractListLoader {

	CableDListLoader(ZipFile zip) {
		// 320 bytes for E series
		super(zip, "map-CableD", 320);
	}

	protected Channel parseRecord(List<Byte> record) {
		int number = getInt(record, 0);
		if (number <= 0) {
			return null;
		}
		int logicalNumber = getInt(record, 44);
		String name = getString(record, 64, 100);
		ServiceType serviceType = ServiceType.fromType(record.get(15));

		return new Channel(number, logicalNumber, name, serviceType);
	}

}
