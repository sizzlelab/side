/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import android.widget.TextView;
import fi.hut.soberit.physicalactivity.legacy.LegacyStorage;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.graphs.PhysicalActivityGraph;
import fi.hut.soberit.sensors.hxm.HxMDriver;
import fi.hut.soberit.sensors.uploaders.PhysicalActivityUploader;

public class RecordSession extends PhysicalActivityGraph implements OnClickListener {
	
	private boolean hxmMeter;
	private TextView notificationMessageView;

	public RecordSession () {
		this.mainLayout = R.layout.activity_recording_screen;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
    	settingsFileName = Settings.APP_PREFERENCES_FILE;
    	sessionIdPreference = Settings.ACTIVITY_SESSION_IN_PROCESS;
    	startNewSession = true;
    	sessionName = getString(R.string.session_name_physical);

        final SharedPreferences prefs = getSharedPreferences(Settings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		final String meter = prefs.getString(Settings.METER, "");
		Log.d(TAG, "meter " + meter);
		hxmMeter = Settings.METER_HXM.equals(meter);
			
		Log.d(TAG, "meter " + meter + " " + hxmMeter);
    	
		super.onCreate(savedInstanceState);		
		
        final Button stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(this);
        
        notificationMessageView = (TextView) findViewById(R.id.notification_message);

	}	
	
	@Override
	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
		final HxMPulseDriver.Discover hxmDiscover = new HxMPulseDriver.Discover();
		final AntPlusDriver.Discover antDiscover = new AntPlusDriver.Discover();
		
		final AccelerometerDriver.Discover accDiscover = new AccelerometerDriver.Discover(); 

		// types, drivers, uploaders
		allTypes = new ArrayList<ObservationType>();
				
		final ObservationType[] hxmTypes = hxmDiscover.getObservationTypes(this);
		ObservationType[] strapTypes = null;
		Driver strapDriver = null;
		
		if (hxmMeter) {
			strapTypes = hxmDiscover.getObservationTypes(this);
			strapDriver = hxmDiscover.getDriver();
		} else {
			strapTypes = antDiscover.getObservationTypes(this);
			strapDriver = antDiscover.getDriver();
		}
				
						
		final ObservationType[] accTypes = accDiscover.getObservationTypes(this);

		final ArrayList<ObservationType> strapTypesList = new ArrayList<ObservationType>();
		for(ObservationType type: strapTypes) {
			strapTypesList.add(type);
		}
		
		final ArrayList<ObservationType> accTypesList = new ArrayList<ObservationType>();
		for(ObservationType type: accTypes) {
			accTypesList.add(type);
		}
		
		allTypes.addAll(strapTypesList);
		allTypes.addAll(accTypesList);
		
		driverTypes = new HashMap<Driver, ArrayList<ObservationType>>();
		driverTypes.put(strapDriver, strapTypesList);
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
 
		if (hxmMeter) {
			final Intent startHxMDriver = new Intent(this, HxMPulseDriver.class);
			
			startHxMDriver.putExtra(BroadcastingService.INTENT_BROADCAST_FREQUENCY, 
					Long.parseLong(prefs.getString(
						Settings.BROADCAST_FREQUENCY,
						getString(R.string.broadcast_frequency_default))));
			startHxMDriver.putExtra(HxMDriver.INTENT_DEVICE_ADDRESS, prefs.getString(Settings.HXM_BLUETOOTH_ADDRESS, ""));
			startHxMDriver.putExtra(HxMDriver.INTENT_TIMEOUT, Integer.parseInt(prefs.getString(Settings.TIMEOUT, "0")));
			
			startService(startHxMDriver);
		} else {
			
			final Intent startPulseDriver = new Intent(this, AntPlusDriver.class);
	
			startPulseDriver.putExtra(BroadcastingService.INTENT_BROADCAST_FREQUENCY, 
					Long.parseLong(prefs.getString(
						Settings.BROADCAST_FREQUENCY,
						getString(R.string.broadcast_frequency_default))));
	
			startService(startPulseDriver);	
		}
		
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
		
		final Intent startStorage = new Intent(this, LegacyStorage.class);
		
		startStorage.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, allTypes);
		startStorage.putExtra(DriverInterface.INTENT_SESSION_ID, sessionId);
		
		startService(startStorage);
	}
	
	@Override
	protected void stopSession() {
		super.stopSession();
	
		if (hxmMeter) {
			final Intent stopHxMDriver = new Intent(this, HxMPulseDriver.class);
			stopService(stopHxMDriver);	
		} else {
			final Intent stopPulseDriver = new Intent(this, AntPlusDriver.class);
			stopService(stopPulseDriver);	
		}
		
		final Intent stopAccelerometerDriver = new Intent(this, AccelerometerDriver.class);
		stopService(stopAccelerometerDriver);
		
		final Intent stopUploader = new Intent(this, PhysicalActivityUploader.class);
		stopService(stopUploader);
		
		final Intent stopStorage = new Intent(this, LegacyStorage.class);
		stopService(stopStorage);
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
	
	protected void onPulseObservation(int pulse) {
	
		if (pulse == 0) {
			notificationMessageView.setText(R.string.check_heart_beat_meter);
		} else {
			notificationMessageView.setText("");
		}
	}
}
