package fi.hut.soberit.sensors.fora;

public interface SensorStatusController {

	public void startSession(String driverAction);
	
	public void stopSession(String driverAction);

	public int getSensorStatus(String driverAction);
	
	public boolean isSensorPaired(String driverAction);
}
