package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.util.List;
import java.util.zip.ZipFile;

import pl.danlz.remotecontrol.samsung.channellist.Channel;
import pl.danlz.remotecontrol.samsung.channellist.Channel.ServiceType;

/**
 * Loader for {@code map-SateD} list.
 * 
 * @author Leszek
 */
public class SateDListLoader extends AbstractListLoader {

	SateDListLoader(ZipFile zip) {
		// 168 bytes for E series
		super(zip, "map-SateD", 168);
	}

	@Override
	protected Channel parseRecord(List<Byte> record) {
		byte inUse = record.get(7);
		byte deleted = record.get(8);
		if (inUse == 0  || deleted == 1) {
			return null;
		}
		int number = getInt(record, 0);
		int logicalNumber = getInt(record, 20); // SatelliteIndex
		String name = getString(record, 36, 100);
		ServiceType serviceType = ServiceType.fromType(record.get(14));

		return new Channel(number, logicalNumber, name, serviceType);
	}
}
