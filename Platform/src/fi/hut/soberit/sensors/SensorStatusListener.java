package fi.hut.soberit.sensors;

public interface SensorStatusListener {
	
	int SENSOR_CONNECTED = 1;
	int SENSOR_DISCONNECTED = 2;
	
	public void onSensorStatusChanged(DriverConnection connection, int newStatus);
}
