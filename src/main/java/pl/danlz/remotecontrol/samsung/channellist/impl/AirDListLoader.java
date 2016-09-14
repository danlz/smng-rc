package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.util.zip.ZipFile;

/**
 * Loader for {@code map-AirD} list.
 * 
 * @author Leszek
 */
class AirDListLoader extends AbstractDListLoader {

	AirDListLoader(ZipFile zip) {
		super(zip, "map-AirD");
	}

}
