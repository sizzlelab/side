package fi.hut.soberit.antpulse;

import java.util.ArrayList;

import android.content.Context;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.services.StorageService;
import fi.hut.soberit.sensors.storage.GenericObservationStorage;

public class AntStorage extends GenericObservationStorage {

	public static final String ACTION = AntStorage.class.getName();
	
	public static class Discover extends StorageService.Discover {
		public Storage[] getStorages(Context context) {
			Storage [] storages = new Storage[2];
			
			ObservationType pulseType = null;
			
			AntPulseDriver.Discover discover = new AntPulseDriver.Discover();
			for(ObservationType type: discover.getObservationTypes(context)) {
				if (DriverInterface.TYPE_PULSE.equals(type.getMimeType())) {
					pulseType = type;
					break;
				}
			}
			
			storages[0] = new Storage(1311060162999l, ACTION);
			
			ArrayList<ObservationType> types = new ArrayList<ObservationType>();
			storages[0].setTypes(types);
			
			types.add(pulseType);
			
			storages[1] = GenericObservationStorage.STORAGE;
			storages[1].setTypes(types);
			
			return storages;
		}
	}
}
