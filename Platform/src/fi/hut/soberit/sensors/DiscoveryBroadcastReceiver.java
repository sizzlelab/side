package fi.hut.soberit.sensors;

import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

public class DiscoveryBroadcastReceiver extends BroadcastReceiver {
	
	public static final String TAG = DiscoveryBroadcastReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		
		final Intent response = new Intent();
		response.setAction(DriverInterface.ACTION_DISCOVERED);
		response.putExtra(DriverInterface.INTENT_DRIVER_SERVICE_URL, AccelerometerDriver.class.getName());
		
		final ObservationKeyname [] keynames = new ObservationKeyname [] {
			new ObservationKeyname("X", "ms^2", "double"),
			new ObservationKeyname("Y", "ms^2", "double"),
			new ObservationKeyname("Z", "ms^2", "double"),
		};
		
		final ObservationType type = new ObservationType(
				"Accelerometer", 
				"application/vnd.sensor.accelerometer",
				"Internal device sensor", 
				keynames);
		
		response.putExtra(DriverInterface.INTENT_DATA_TYPE, (Parcelable)type);	
		response.putExtra(DriverInterface.INTENT_DEVICE_ID, "Internal on-board sensors #1");
		
		context.sendBroadcast(response);
	}	
}
