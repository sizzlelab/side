package fi.hut.soberit.sensors.fora;

public interface SensorStatusController {

	public void startSession(long typeId);
	
	public void stopSession(long typeId);

	public int getSensorStatus(long typeId);
}
