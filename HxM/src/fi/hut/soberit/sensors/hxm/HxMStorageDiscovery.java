package fi.hut.soberit.sensors.hxm;

import java.util.ArrayList;

import android.content.Context;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.services.StorageService.Discover;
import fi.hut.soberit.sensors.storage.GenericObservationStorage;

public class HxMStorageDiscovery extends Discover {

	public Storage[] getStorages(Context context) {
		
		final HxMDriver.Discover discover = new HxMDriver.Discover();
		
		final Storage storage = GenericObservationStorage.STORAGE;
		ArrayList<ObservationType> types = new ArrayList<ObservationType>();
		storage.setTypes(types);
		
		for(ObservationType type: discover.getObservationTypes(context)) {
			if (DriverInterface.TYPE_PULSE.equals(type.getMimeType())) {
				types.add(type);
				break;
			}
		}
		
		return new Storage[] {storage};
	}
}
