package fi.hut.soberit.sensors;

import java.util.List;
import fi.hut.soberit.sensors.generic.GenericObservation;

public interface DriverWithDB {
	
	public List<GenericObservation> getObservations(int sessionId, long start, long end);
}
