package fi.hut.soberit.sensors;

import java.util.List;

public class DriverInfo {
	
	private long driverId;
	
	private String url;
	
	private List<ObservationTypeShort> observationTypes;
	
	public DriverInfo(long driverId, String url, List<ObservationTypeShort> observationTypes) {
		this.driverId = driverId;
		this.url = url;
		this.observationTypes = observationTypes;
	}	
	
	public List<ObservationTypeShort> getObservationTypes() {
		return observationTypes;
	}

	public void setObservationTypes(List<ObservationTypeShort> observationTypes) {
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

	public ObservationTypeShort getObservationType(int typeId) {
		return null;
	}
}
