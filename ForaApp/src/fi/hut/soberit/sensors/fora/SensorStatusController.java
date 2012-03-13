package fi.hut.soberit.sensors.fora;

public interface SensorStatusController {

	public void connect(String driverAction);
	
	public void disconnect(String driverAction);

	public int getSensorStatus(String driverAction);
}
