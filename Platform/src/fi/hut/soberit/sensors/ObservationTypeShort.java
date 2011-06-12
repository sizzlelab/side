package fi.hut.soberit.sensors;

class ObservationTypeShort implements Comparable<ObservationTypeShort>{
	
	long observationTypeId;
	long driverId;
	
	String mimeType;
	String name;
	
	long[] keynames;
	private boolean enabled;

	public ObservationTypeShort(long id, long driverId, String name, String mime, long[] keynames, boolean enabled) {
		this.observationTypeId = id;
		this.driverId = driverId;
		this.name = name;
		this.mimeType = mime;
		this.keynames = keynames;		
		this.enabled = enabled;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return observationTypeId;
	}

	public void setObservationTypeId(long observationTypeId) {
		this.observationTypeId = observationTypeId;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public int getKeynamesNum() {
		return keynames.length;
	}

	public long[] getKeynames() {
		return keynames;
	}

	public void setKeynames(long[] keynames) {
		this.keynames = keynames;
	}

	public long getDriverId() {
		return driverId;
	}

	public void setDriverId(long driverId) {
		this.driverId = driverId;
	}

	@Override
	public int compareTo(ObservationTypeShort another) {
		if (observationTypeId < another.getId()) {
			return -1;
		} else if (observationTypeId > another.getId()) {
			return 1;
		} 
		
		return 0;
	}

	public boolean getEnabled() {
		return enabled;
	}
}