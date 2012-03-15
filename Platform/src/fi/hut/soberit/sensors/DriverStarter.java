package fi.hut.soberit.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DriverStarter extends BroadcastReceiver implements DriverStatusListener {

	protected final String TAG = this.getClass().getSimpleName();
	
	private SinkDriverConnection connection;

	private String address;
	
	public DriverStarter(SinkDriverConnection conn, String address) {
		this.connection = conn;
		this.address = address;		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive " + connection.getDriverAction() );
		
		final String driverUrl = BroadcastingService.decodePingBackAction(intent.getAction());

		if (!connection.getDriverAction().equals(driverUrl)) {
			return;
		}
		
		connection.bind(context);
		
		connection.addDriverStatusListener(this);
	}

	@Override
	public void onDriverStatusChanged(DriverConnection connection, int oldStatus, int newStatus) {
		Log.d(TAG, String.format("%d == %s", newStatus, connection.getDriverAction()));
		
		if (newStatus == DriverStatusListener.BOUND) {
			connection.sendStartConnecting(address);
			// TODO: move driver status listener to common interface
			((SinkDriverConnection) connection).removeDriverStatusListener(this);
		}
	}
}
