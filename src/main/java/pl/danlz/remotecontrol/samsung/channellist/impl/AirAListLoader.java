package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.util.List;
import java.util.zip.ZipFile;

import pl.danlz.remotecontrol.samsung.channellist.Channel;

/**
 * Loader for {@code map-AirA} list.
 * 
 * @author Leszek
 */
class AirAListLoader extends AbstractListLoader {

	AirAListLoader(ZipFile zip) {
		// 64 bytes for E series
		super(zip, "map-AirA", 64);
	}

	@Override
	protected Channel parseRecord(List<Byte> record) {
		byte inUse = record.get(1);
		byte deleted = record.get(2);
		if (inUse == 0 || deleted == 1) {
			return null;
		}
		int number = getInt(record, 9);
		int logicalNumber = getInt(record, 16); // slotNumber
		String name = getString(record, 20, 10);

		return new Channel(number, logicalNumber, name, null);
	}

}
