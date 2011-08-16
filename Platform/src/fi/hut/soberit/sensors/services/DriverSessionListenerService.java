package fi.hut.soberit.sensors.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import fi.hut.soberit.sensors.DriverInterface;

public abstract class DriverSessionListenerService extends DriverListenerService {

	private IntentFilter sessionStopBroadcastFilter;
	private StopSessionReceiver stopSessionReceiver;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		int res = super.onStartCommand(intent, flags, startId);
		if (intent == null) {
			return res;
		}
		
		sessionStopBroadcastFilter = new IntentFilter();
		sessionStopBroadcastFilter.addAction(DriverInterface.ACTION_SESSION_STOP);
		
		stopSessionReceiver = new StopSessionReceiver();
		registerReceiver(stopSessionReceiver, sessionStopBroadcastFilter);
		
		return res;
    }
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		
		if (sessionStopBroadcastFilter != null) {
			unregisterReceiver(stopSessionReceiver);
		}
	}
	
	
	public class StopSessionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive" + intent.getAction());
			
			DriverSessionListenerService.this.stopSelf();
		}
	}
}
