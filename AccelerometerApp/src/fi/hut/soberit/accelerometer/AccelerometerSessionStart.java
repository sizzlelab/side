/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * Maksim Golivkin <maksim@golivkin.eu>
 ******************************************************************************/
package fi.hut.soberit.accelerometer;

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
import fi.hut.soberit.sensors.drivers.AccelerometerDriver;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Uploader;


public class AccelerometerSessionStart extends SessionBroadcastReceiver {

	@Override
	protected void startDriverService(Context context, Intent startSessionIntent, Driver driver) {
		Log.d(TAG, "startDriverService");
		final Intent startDriver = new Intent();
		
		final SharedPreferences prefs = context.getSharedPreferences(
				AccelerometerDriverSettings.APP_PREFERENCES_FILE, 
				Context.MODE_PRIVATE);
		
		int delay = AccelerometerDriverSettings.stringDelayToConstant(
				context, 
				prefs.getString(AccelerometerDriverSettings.RECORDING_DELAY, 
				context.getString(R.string.recording_delay_default)));
		startDriver.putExtra(AccelerometerDriver.SENSOR_SERVICE, delay);
		
		long recordingFreq = Long.parseLong(prefs.getString(
				AccelerometerDriverSettings.RECORDING_FREQUENCY, 
				context.getString(R.string.recording_frequency_default)));
		
		long broadcastFreq = Long.parseLong(prefs.getString(
				AccelerometerDriverSettings.BROADCAST_FREQUENCY, 
				context.getString(R.string.broadcast_frequency_default)));
		startDriver.putExtra(BroadcastingService.INTENT_BROADCAST_FREQUENCY, broadcastFreq);		
		
		Log.d(TAG, "delay = " + delay + " recordingFreq = " + recordingFreq + " broadcastFreq = " + broadcastFreq);
		
		startDriver.setAction(driver.getUrl());
		context.startService(startDriver);
	}
	
	@Override
	protected void startUploaderService(Uploader uploader, Context context, Intent broadcastIntent, ArrayList<ObservationType> types) {
		Log.d(TAG, "startUploaderService");
		final Intent startUploader = new Intent();
		startUploader.setAction(uploader.getUrl());
		
		startUploader.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, types);
		
		final SharedPreferences prefs = context.getSharedPreferences(
				AccelerometerDriverSettings.APP_PREFERENCES_FILE, 
				Activity.MODE_PRIVATE);
		
		startUploader.putExtra(AccelerometerAxisUploader.INTENT_AHL_URL, prefs.getString(AccelerometerDriverSettings.AHL_URL, ""));
		startUploader.putExtra(AccelerometerAxisUploader.INTENT_USERNAME, prefs.getString(AccelerometerDriverSettings.USERNAME, ""));
		startUploader.putExtra(AccelerometerAxisUploader.INTENT_PASSWORD, prefs.getString(AccelerometerDriverSettings.PASSWORD, ""));
		startUploader.putExtra(AccelerometerAxisUploader.INTENT_WEBLET, prefs.getString(AccelerometerDriverSettings.WEBLET, ""));
		
		context.startService(startUploader);
	}
	
	@Override
	protected boolean isInterestingDriver(Driver driver) {

		boolean matched = AccelerometerDriver.ACTION.equals(driver.getUrl());
		Log.d(TAG, String.format("matched ? [%s, %b]", driver.getUrl(), matched));

		return matched;
	}

	@Override
	protected boolean isInterestingUploader(Uploader driver) {

		boolean matched = AccelerometerAxisUploader.UPLOADER_ACTION.equals(driver.getUrl());
		Log.d(TAG, String.format("matched ? [%s, %b]", driver.getUrl(), matched));

		return matched;
	}

}
