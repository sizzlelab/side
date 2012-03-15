package fi.hut.soberit.sensors;

import android.bluetooth.BluetoothSocket;

public class ConnectionDetails {
	public static final int DISCONNECTED = 0;
	public static final int CONNECTING = 1;
	public static final int CONNECTED = 2;

	int status = DISCONNECTED;

	BluetoothSocket socket;

	boolean stopConnecting = false;
	
	String address;

	String orginator;
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public BluetoothSocket getSocket() {
		return socket;
	}

	public void setSocket(BluetoothSocket socket) {
		this.socket = socket;
	}

	public boolean isStopConnecting() {
		return stopConnecting;
	}

	public void setStopConnecting(boolean stopConnecting) {
		this.stopConnecting = stopConnecting;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getOrginator() {
		return orginator;
	}

	public void setOrginator(String orginator) {
		this.orginator = orginator;
	}
}