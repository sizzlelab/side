package fi.hut.soberit.sensors.generic;

public class StorageType {

	private long storageId;
	private long observationTypeId;
	
	public StorageType(long storageId, long observationTypeId) {
		super();
		this.storageId = storageId;
		this.observationTypeId = observationTypeId;
	}
	
	public long getStorageId() {
		return storageId;
	}
	public void setStorageId(long storageId) {
		this.storageId = storageId;
	}
	public long getObservationTypeId() {
		return observationTypeId;
	}
	public void setObservationTypeId(long observationTypeId) {
		this.observationTypeId = observationTypeId;
	}
	
}
