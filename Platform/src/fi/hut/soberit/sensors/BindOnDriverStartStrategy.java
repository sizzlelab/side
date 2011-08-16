package fi.hut.soberit.sensors;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BindOnDriverStartStrategy extends BroadcastReceiver {
	
	private static final String TAG = BindOnDriverStartStrategy.class.getSimpleName();
	
	private ArrayList<DriverConnection> connections;

	public BindOnDriverStartStrategy(ArrayList<DriverConnection> connections) {
		this.connections = connections;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "PingBackRecever.onReceive " + intent.getAction());
		final String action = intent.getAction();
		
		final String driverUrl = action.substring(0, action.length() - BroadcastingService.STARTED_PREFIX.length());
		
		for(DriverConnection connection: connections) {
			if (!driverUrl.equals(connection.getDriver().getUrl())) {
				continue;
			}
			
			Log.d(TAG, "Binding to " + connection.getDriver().getUrl());
			connection.bind(context);
			return;
		}
	}		
}
