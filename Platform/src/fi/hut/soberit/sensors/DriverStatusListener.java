package fi.hut.soberit.sensors;

public interface DriverStatusListener {
	
	int UNBOUND = 21;
	int BOUND = 22;
	
	int CONNECTING = 23;
	int CONNECTED = 24;
	
	int COUNTING = 12;
	int DOWNLOADING = 11;
	
	public void onDriverStatusChanged(DriverConnection connection, int newStatus);
}
