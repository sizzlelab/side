package fi.hut.soberit.sensors;

public interface DriverStatusListener {
	
	int UNBOUND = 21;
	int BOUND = 22;
	
	int CONNECTING = 23;
	int CONNECTED = 24;
	
	int COUNTING = 25;
	int DOWNLOADING = 26;
	
	public void onDriverStatusChanged(DriverConnection connection, int oldStatus, int newStatus);
}
