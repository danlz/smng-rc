package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.util.List;
import java.util.zip.ZipFile;

import pl.danlz.remotecontrol.samsung.channellist.Channel;
import pl.danlz.remotecontrol.samsung.channellist.Channel.ServiceType;

/**
 * Base class for digital channel list loaders.
 * 
 * @author Leszek
 */
public class AbstractDListLoader extends AbstractListLoader {

	AbstractDListLoader(ZipFile zip, String entryName) {
		// 320 bytes for E series
		super(zip, entryName, 320);
	}

	protected Channel parseRecord(List<Byte> record) {
		int number = getInt(record, 0);
		byte deleted = record.get(8);
		if (number <= 0 || deleted == 1) {
			return null;
		}
		int logicalNumber = getInt(record, 44);
		String name = getString(record, 64, 100);
		ServiceType serviceType = ServiceType.fromType(record.get(15));

		return new Channel(number, logicalNumber, name, serviceType);
	}

}
