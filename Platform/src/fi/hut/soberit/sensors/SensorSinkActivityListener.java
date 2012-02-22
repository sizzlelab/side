package fi.hut.soberit.sensors;

public interface SensorSinkActivityListener {

	int DISCONNECTED = 1;
	int CONNECTING = 2;
	int CONNECTED = 3;
	int DOWNLOADING = 4;
	
	
	public void onSensorSinkStatusChanged(DriverConnection connection, int newStatus);
}
