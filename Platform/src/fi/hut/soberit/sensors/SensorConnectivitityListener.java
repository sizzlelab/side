package fi.hut.soberit.sensors;

public interface SensorConnectivitityListener {
	
	public void onSensorConnectivityChanged(DriverConnection connection, int newStatus);
}
