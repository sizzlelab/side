package fi.hut.soberit.sensors.hxm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.SessionBroadcastReceiver;
import fi.hut.soberit.sensors.generic.Uploader;

public class HxMSessionStart extends SessionBroadcastReceiver {

	boolean startOnce = false;
	
	@Override
	protected void startDriverService(Context context, Intent broadcastIntent, Driver driver) {
		final Intent startDriver = new Intent();
		
		final SharedPreferences prefs = context.getSharedPreferences(
				HxMPulseSettings.APP_PREFERENCES_FILE, 
				Context.MODE_PRIVATE);
				
		startDriver.putExtra(BroadcastingService.INTENT_BROADCAST_FREQUENCY, 
				Long.parseLong(prefs.getString(
					HxMPulseSettings.BROADCAST_FREQUENCY,
					context.getString(R.string.broadcast_frequency_default))));

		startDriver.putExtra(HxMDriver.INTENT_TIMEOUT, 
				Integer.parseInt(prefs.getString(
						HxMPulseSettings.TIMEOUT, "")));
		
		startDriver.putExtra(HxMDriver.INTENT_DEVICE_ADDRESS, 
				prefs.getString(HxMPulseSettings.BLUETOOTH_DEVICE_ADDRESS, ""));
		
		startDriver.setAction(HxMDriver.ACTION);
		context.startService(startDriver);
	}
	
	
	@Override
	protected boolean isInterestingUploader(Uploader uploader) {

		return false;
	}
	
	@Override
	protected boolean isInterestingDriver(Driver driver) {

		boolean matched = HxMDriver.ACTION.equals(driver.getUrl());
		Log.d(TAG, String.format("matched ? [%s, %b]", driver.getUrl(), matched));

		return matched;
	}

}