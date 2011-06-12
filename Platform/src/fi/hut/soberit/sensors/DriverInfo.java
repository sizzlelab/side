package fi.hut.soberit.sensors;

import java.util.List;

import fi.hut.soberit.sensors.generic.ObservationType;

public class DriverInfo implements Comparable<DriverInfo> {
	
	private long driverId;
	
	private String url;

	private List<ObservationType> observationTypes;
	
	public DriverInfo(long driverId, String url, List<ObservationType> observationTypes) {
		this.driverId = driverId;
		this.url = url;
		this.observationTypes = observationTypes;
	}	
	
	public List<ObservationType> getObservationTypes() {
		return observationTypes;
	}

	public void setObservationTypes(List<ObservationType> observationTypes) {
		this.observationTypes = observationTypes;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	
	public long getId() {
		return driverId;
	}
	
	public void setId(long id) {
		this.driverId = id;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof DriverInfo)) {
			return false;
		}
		
		return driverId == ((DriverInfo)o).getId();
	}

	public int compareTo(DriverInfo object2) {
		long id2 = object2.getId();
		
		if (driverId < id2) {
			return -1;
		}
		
		if (driverId > id2) {
			return 1;
		}
		
		return 0;
	}
}
