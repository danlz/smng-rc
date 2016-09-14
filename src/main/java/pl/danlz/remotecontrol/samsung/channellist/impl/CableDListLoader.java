package pl.danlz.remotecontrol.samsung.channellist.impl;

import java.util.zip.ZipFile;

/**
 * Loader for {@code map-CableD} list.
 * 
 * @author Leszek
 */
class CableDListLoader extends AbstractDListLoader {

	CableDListLoader(ZipFile zip) {
		super(zip, "map-CableD");
	}

}
