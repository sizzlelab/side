package fi.hut.soberit.sensors;

class ObservationTypeShort {
	
	long observationTypeId;
	String mimeType;
	
	long[] keynames;

	public ObservationTypeShort(long id, String mime, long[] keynames) {
		this.observationTypeId = id;
		this.mimeType = mime;
		this.keynames = keynames;		
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
}