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
package fi.hut.soberit.physicalactivity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.graphs.PhysicalActivityGraph;
import fi.hut.soberit.sensors.uploaders.PhysicalActivityUploader;

public class RecordSession extends PhysicalActivityGraph implements OnClickListener {
	
	public RecordSession () {
		this.mainLayout = R.layout.activity_recording_screen;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
    	settingsFileName = Settings.APP_PREFERENCES_FILE;
    	sessionIdPreference = Settings.ACTIVITY_SESSION_IN_PROCESS;
    	startNewSession = true;
    	
		
		super.onCreate(savedInstanceState);		
		
        final Button stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(this);
	}	
	
	@Override
	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
		final HxMPulseDriver.Discover hxmDiscover = new HxMPulseDriver.Discover();
		final AccelerometerDriver.Discover accDiscover = new AccelerometerDriver.Discover(); 

		// types, drivers, uploaders
		allTypes = new ArrayList<ObservationType>();
				
		final ObservationType[] hxmTypes = hxmDiscover.getObservationTypes(this);
		final ObservationType[] accTypes = accDiscover.getObservationTypes(this);

		final ArrayList<ObservationType> hxmTypesList = new ArrayList<ObservationType>();
		for(ObservationType type: hxmTypes) {
			hxmTypesList.add(type);
		}
		
		final ArrayList<ObservationType> accTypesList = new ArrayList<ObservationType>();
		for(ObservationType type: accTypes) {
			accTypesList.add(type);
		}
		
		allTypes.addAll(hxmTypesList);
		allTypes.addAll(accTypesList);
		
		driverTypes = new HashMap<Driver, ArrayList<ObservationType>>();
		driverTypes.put(hxmDiscover.getDriver(), hxmTypesList);
		driverTypes.put(accDiscover.getDriver(), accTypesList);
		
		
		for(ObservationType type: allTypes) {
			if (type.getMimeType().equals(DriverInterface.TYPE_PULSE)) {
				pulseType = type;
			} else if (type.getMimeType().equals(DriverInterface.TYPE_ACCELEROMETER)) {
				accelerometerType = type;
			}
		}		
	}

	@Override
	protected void onStartSession() {
		Log.d(TAG, "onStartSession");
		
		final SharedPreferences prefs = getSharedPreferences(
				settingsFileName, 
				MODE_PRIVATE);
		
		final Intent startPulseDriver = new Intent(this, HxMPulseDriver.class);

		startPulseDriver.putExtra(BroadcastingService.INTENT_BROADCAST_FREQUENCY, 
				Long.parseLong(prefs.getString(
					Settings.BROADCAST_FREQUENCY,
					getString(R.string.broadcast_frequency_default))));
		startPulseDriver.putExtra(HxMPulseDriver.INTENT_DEVICE_ADDRESS, 
				prefs.getString(Settings.BLUETOOTH_DEVICE_ADDRESS, ""));
		startPulseDriver.putExtra(HxMPulseDriver.INTENT_TIMEOUT,
				Integer.parseInt(prefs.getString(Settings.TIMEOUT, "")));
		
		startService(startPulseDriver);	
		
		final Intent startAccelerometerDriver = new Intent(this, AccelerometerDriver.class);
		int delay = Settings.stringDelayToConstant(this, 
				prefs.getString(Settings.RECORDING_DELAY, 
				getString(R.string.recording_delay_default)));
		startAccelerometerDriver.putExtra(AccelerometerDriver.SENSOR_SERVICE, delay);
		
		long recordingFreq = Long.parseLong(prefs.getString(
				Settings.RECORDING_FREQUENCY, 
				getString(R.string.recording_frequency_default)));
				
		long broadcastFreq = Long.parseLong(prefs.getString(
				Settings.BROADCAST_FREQUENCY, 
				getString(R.string.broadcast_frequency_default)));
		startAccelerometerDriver.putExtra(BroadcastingService.INTENT_BROADCAST_FREQUENCY, broadcastFreq);		
		
		Log.d(TAG, "delay = " + delay + " recordingFreq = " + recordingFreq + " broadcastFreq = " + broadcastFreq);
		
		startService(startAccelerometerDriver);
		
		final Intent startUploader = new Intent(this, PhysicalActivityUploader.class);
		
		startUploader.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, allTypes);
		
		startUploader.putExtra(PhysicalActivityUploader.INTENT_AHL_URL, prefs.getString(Settings.AHL_URL, ""));
		startUploader.putExtra(PhysicalActivityUploader.INTENT_USERNAME, prefs.getString(Settings.USERNAME, ""));
		startUploader.putExtra(PhysicalActivityUploader.INTENT_PASSWORD, prefs.getString(Settings.PASSWORD, ""));
		startUploader.putExtra(PhysicalActivityUploader.INTENT_WEBLET, prefs.getString(Settings.WEBLET, ""));
		
		startService(startUploader);
	}
	
	@Override
	protected void stopSession() {
		super.stopSession();
	
		final Intent startPulseDriver = new Intent(this, HxMPulseDriver.class);
		stopService(startPulseDriver);	
		
		final Intent startAccelerometerDriver = new Intent(this, AccelerometerDriver.class);
		stopService(startAccelerometerDriver);
		
		final Intent startUploader = new Intent(this, PhysicalActivityUploader.class);
		stopService(startUploader);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	
		dbHelper.closeDatabases();
	}

	@Override
	public void onClick(View v) {
		stopSession();

		
		finish();
	}
}
