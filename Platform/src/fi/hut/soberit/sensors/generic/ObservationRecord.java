package fi.hut.soberit.sensors.generic;

public interface ObservationRecord {

	public long getTime();
	
	public byte[] getValue(int i);
}
