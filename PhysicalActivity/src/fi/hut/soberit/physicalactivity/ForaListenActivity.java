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


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import eu.mobileguild.ui.MultidimensionalArrayAdapter;
import eu.mobileguild.utils.DataTypes;
import fi.hut.soberit.fora.ForaDriver;
import fi.hut.soberit.fora.db.BloodPressureDao;
import fi.hut.soberit.fora.db.Glucose;
import fi.hut.soberit.fora.db.GlucoseDao;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationValueDao;
import fi.hut.soberit.sensors.activities.BroadcastListenerActivity;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.uploaders.PhysicalActivityUploader;

public class ForaListenActivity extends BroadcastListenerActivity implements OnClickListener {
	private static final String TAG = ForaListenActivity.class.getSimpleName();

	private MultidimensionalArrayAdapter listAdapter;
	private ObservationType pulseType;
	private ObservationType glucoseType;
	private ObservationType bloodPressureType;


	private ObservationValueDao observationValueDao;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

	private ArrayList<String[]> observations;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	settingsFileName = Settings.APP_PREFERENCES_FILE;
    	sessionIdPreference = Settings.VITAL_SESSION_IN_PROCESS;
    	startNewSession = true;
    	registerInDatabase = false;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fora_listen);
        
		final Button stopButton = (Button)findViewById(R.id.stop_button);
		stopButton.setOnClickListener(this);   
		
		final ListView listView = (ListView)findViewById(android.R.id.list);
		
		observations = new ArrayList<String[]>();
		
		listAdapter = new MultidimensionalArrayAdapter(
				this, 
        		R.layout.vital_param_item, 
        		new int [] {R.id.type, R.id.time, R.id.values},
        		observations);
		listView.setAdapter(listAdapter);

		
		observationValueDao = new ObservationValueDao(dbHelper);
		refreshListView();
    }

	protected void refreshListView() {
		observations.clear();
		
		for(GenericObservation observation: observationValueDao.getAll()) {
			observations.add(observationToStringArray(observation));
		}
		
		listAdapter.notifyDataSetChanged();
	}

	private String[] observationToStringArray(GenericObservation observation) {
		String [] item = null;
		if (observation.getObservationTypeId() == bloodPressureType.getId()) {
			item = new String[] {
					"Blood pressure",
					dateFormat.format(observation.getTime()),
					String.format("systolic: %d mmHg, diastolic: %d mmHg", 
							DataTypes.byteArrayToInt(observation.getValue(), 0),
							DataTypes.byteArrayToInt(observation.getValue(), 4))
			};
			
		} else if (observation.getObservationTypeId() == glucoseType.getId()) {
			
			int glucoseValue = DataTypes.byteArrayToInt(observation.getValue(), 0);
			int type = DataTypes.byteArrayToInt(observation.getValue(), 4);
			
			item = new String[] {
					"Glucose",
					dateFormat .format(observation.getTime()),
					String.format("glucose: %d mg/dl, type: %d", glucoseValue, type)
			};
			
		} else if (observation.getObservationTypeId() == pulseType.getId()) {
			item = new String[] {
					"Pulse",
					dateFormat.format(observation.getTime()),
					String.format("pulse: %d bpm", 
							DataTypes.byteArrayToInt(observation.getValue(), 0))
			};
		}		
		
		return item;
	}

	@Override
	protected void onStartSession() {		

	
		final Intent foraDriverIntent = new Intent();
		foraDriverIntent.setAction(ForaDriver.ACTION);
		foraDriverIntent.putExtra(ForaDriver.INTENT_DEVICE_ADDRESS, "00:12:A1:B0:40:49");
		foraDriverIntent.putExtra(ForaDriver.INTENT_BROADCAST_FREQUENCY, 0l);
		
		startService(foraDriverIntent);			
		
		final SharedPreferences prefs = getSharedPreferences(
				settingsFileName, 
				MODE_PRIVATE);
		
		final Intent uploaderService = new Intent();
		uploaderService.setAction(ForaUploader.ACTION);
		
		uploaderService.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, allTypes);
				
		uploaderService.putExtra(ForaUploader.INTENT_AHL_URL, prefs.getString(Settings.AHL_URL, ""));
		uploaderService.putExtra(ForaUploader.INTENT_USERNAME, prefs.getString(Settings.VITAL_USERNAME, ""));
		uploaderService.putExtra(ForaUploader.INTENT_PASSWORD, prefs.getString(Settings.VITAL_PASSWORD, ""));
		uploaderService.putExtra(ForaUploader.INTENT_BLOOD_PRESSURE_WEBLET,  prefs.getString(Settings.BLOOD_PRESSURE_WEBLET, null));
		uploaderService.putExtra(ForaUploader.INTENT_GLUCOSE_WEBLET, prefs.getString(Settings.GLUCOSE_WEBLET, null));
		
		startService(uploaderService);
	}
	
	protected void stopSession() {
		super.stopSession();
	
		final Intent stopForaDriver = new Intent(this, ForaDriver.class);
		stopService(stopForaDriver);
		
		final Intent uploaderService = new Intent();
		uploaderService.setAction(ForaUploader.ACTION);
		
		stopService(uploaderService);
	}
	
	@Override
	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
		final ForaDriver.Discover foraDriverDescription = new ForaDriver.Discover();

	
		allTypes = new ArrayList<ObservationType>();
		ObservationType[] types = foraDriverDescription.getObservationTypes(this);
		final ArrayList<ObservationType> foraTypes = new ArrayList<ObservationType>();		
		
		for(ObservationType type: types) {
			if (DriverInterface.TYPE_BLOOD_PRESSURE.equals(type.getMimeType())) {
				bloodPressureType = type;
			} else if (DriverInterface.TYPE_GLUCOSE.equals(type.getMimeType())) {
				glucoseType = type;
			} else if (DriverInterface.TYPE_PULSE.equals(type.getMimeType())) {
				pulseType = type;
			}
			
			allTypes.add(type);
			foraTypes.add(type);			
		}
		
		driverTypes = new HashMap<Driver, ArrayList<ObservationType>>();
		driverTypes.put(foraDriverDescription.getDriver(), foraTypes);
	}
	
	@Override
	public void onClick(View v) {
		stopSession();
		finish();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		dbHelper.closeDatabases();
	}

	@Override
	protected void onReceiveObservations(List<Parcelable> observations) {
		Log.d(TAG, "onReceiveObservations");
		for (Parcelable p: observations) {
			GenericObservation observation = (GenericObservation)p;
			
			Log.d(TAG, "time: " + observation.getTime() + " type: " + observation.getObservationTypeId());
			
			observationValueDao.insertObservationValue(observation);
		}
		
		refreshListView();
	}
	
	
}
