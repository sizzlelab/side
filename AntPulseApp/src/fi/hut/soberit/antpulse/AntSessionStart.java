package fi.hut.soberit.antpulse;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.SessionBroadcastReceiver;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Uploader;
import fi.hut.soberit.sensors.uploaders.PhysicalActivityUploader;

public class AntSessionStart extends SessionBroadcastReceiver {

	@Override
	protected void startDriverService(Context context, Intent broadcastIntent, Driver driver) {
		final Intent startDriver = new Intent();
		
		final SharedPreferences prefs = context.getSharedPreferences(
				AntPulseSettings.APP_PREFERENCES_FILE, 
				Context.MODE_PRIVATE);

		startDriver.putExtra(BroadcastingService.INTENT_BROADCAST_FREQUENCY, 
				Long.parseLong(prefs.getString(
					AntPulseSettings.BROADCAST_FREQUENCY,
					context.getString(R.string.broadcast_frequency_default))));
		
		startDriver.setAction(driver.getUrl());
		context.startService(startDriver);
	}
	
	@Override
	protected void startUploaderService(Uploader uploader, Context context, Intent broadcastIntent, ArrayList<ObservationType> types) {
		final Intent startUploader = new Intent();
		startUploader.setAction(uploader.getUrl());
		
		startUploader.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, types);
		
		final SharedPreferences prefs = context.getSharedPreferences(
				AntPulseSettings.APP_PREFERENCES_FILE, 
				Activity.MODE_PRIVATE);
		
		startUploader.putExtra(PhysicalActivityUploader.INTENT_AHL_URL, prefs.getString(AntPulseSettings.AHL_URL, ""));
		startUploader.putExtra(PhysicalActivityUploader.INTENT_USERNAME, prefs.getString(AntPulseSettings.USERNAME, ""));
		startUploader.putExtra(PhysicalActivityUploader.INTENT_PASSWORD, prefs.getString(AntPulseSettings.PASSWORD, ""));
		startUploader.putExtra(PhysicalActivityUploader.INTENT_WEBLET, prefs.getString(AntPulseSettings.WEBLET, ""));
		
		context.startService(startUploader);
	}
	
	@Override
	protected boolean isInterestingUploader(Uploader uploader) {

		boolean matched = PhysicalActivityUploader.UPLOADER_ACTION.equals(uploader.getUrl());
		Log.d(TAG, String.format("matched ? [%s, %b]", uploader.getUrl(), matched));

		return matched;
	}
	
	@Override
	protected boolean isInterestingDriver(Driver driver) {

		boolean matched = AntPulseDriver.ACTION.equals(driver.getUrl());
		Log.d(TAG, String.format("matched ? [%s, %b]", driver.getUrl(), matched));

		return matched;
	}

}