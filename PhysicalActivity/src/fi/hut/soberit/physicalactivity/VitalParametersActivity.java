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
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import eu.mobileguild.ui.MultidimensionalArrayAdapter;
import eu.mobileguild.utils.LittleEndian;
import fi.hut.soberit.fora.D40Sink;
import fi.hut.soberit.fora.IR21Sink;
import fi.hut.soberit.physicalactivity.legacy.LegacyStorage;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationValueDao;
import fi.hut.soberit.sensors.activities.SinkListenerActivity;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;

public class VitalParametersActivity extends SinkListenerActivity  {
	
	private static final String TAG = VitalParametersActivity.class.getSimpleName();

	private static final String SIS_COUNT = "count";

	protected static final long SLEEP = 0;


	private MultidimensionalArrayAdapter listAdapter;
	private ObservationType pulseType;
	private ObservationType glucoseType;
	private ObservationType bloodPressureType;
	private ObservationType temperatureType;
	private ObservationType ambientType;

	private ObservationValueDao observationValueDao;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

	private ArrayList<String[]> observations;

	private boolean backButtonPressed;

	private int count = 0;
	
    @Override
    public void onCreate(Bundle sis) {

    	startNewSession = true;
    	registerInDatabase = true;

    	super.onCreate(sis);
    	
    	sessionHelper.setPreferencesFilename(Settings.APP_PREFERENCES_FILE);
    	sessionHelper.setSessionIdPreference(Settings.VITAL_SESSION_IN_PROCESS);
    	sessionHelper.setSessionName(getString(R.string.session_name_vital));
    	
        setContentView(R.layout.fora_listen);
        		
		final ListView listView = (ListView)findViewById(android.R.id.list);
		
		observations = new ArrayList<String[]>();
		
		listAdapter = new MultidimensionalArrayAdapter(
				this, 
        		R.layout.vital_param_item, 
        		new int [] {R.id.type, R.id.time, R.id.values},
        		observations);
		listView.setAdapter(listAdapter);

		if (getIntent() == null) {
			count = sis.getInt(SIS_COUNT);
		}
		
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
							LittleEndian.readInt(observation.getValue(), 0),
							LittleEndian.readInt(observation.getValue(), 4))
			};
			
		} else if (observation.getObservationTypeId() == glucoseType.getId()) {
			
			int glucoseValue = LittleEndian.readInt(observation.getValue(), 0);
			int type = LittleEndian.readInt(observation.getValue(), 4);
			
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
							LittleEndian.readInt(observation.getValue(), 0))
			};
		} else if (observation.getObservationTypeId() == temperatureType.getId()) {
			item = new String [] {
					"Temperature",
					"unknown",
					String.format(" %.1f C", 
							LittleEndian.readFloat(observation.getValue(), 0))
			};
		} else if (observation.getObservationTypeId() == ambientType.getId()) {
			item = new String [] {
					"Ambient",
					"unknown",
					String.format(" %.1f C", 
							LittleEndian.readFloat(observation.getValue(), 0))
			};			
		}
		
		return item;
	}

	@Override
	protected void onStartSession() {		

		final SharedPreferences prefs = getSharedPreferences(
				Settings.APP_PREFERENCES_FILE, 
				MODE_PRIVATE);
	
		final Intent foraD40SinkIntent = new Intent();
		foraD40SinkIntent.setAction(D40Sink.ACTION);
		foraD40SinkIntent.putExtra(D40Sink.INTENT_DEVICE_ADDRESS, prefs.getString(Settings.D40_BLUETOOTH_ADDRESS, null));
		
		startService(foraD40SinkIntent);			

		final Intent foraIR21SinkIntent = new Intent();
		foraIR21SinkIntent.setAction(IR21Sink.ACTION);
		foraIR21SinkIntent.putExtra(IR21Sink.INTENT_DEVICE_ADDRESS, prefs.getString(Settings.IR21_BLUETOOTH_ADDRESS, null));
		
		startService(foraIR21SinkIntent);			


		final Intent uploaderService = new Intent();
		uploaderService.setAction(ForaUploader.ACTION);
		
		uploaderService.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, allTypes);
				
		uploaderService.putExtra(ForaUploader.INTENT_AHL_URL, prefs.getString(Settings.AHL_URL, ""));
		uploaderService.putExtra(ForaUploader.INTENT_USERNAME, prefs.getString(Settings.VITAL_USERNAME, ""));
		uploaderService.putExtra(ForaUploader.INTENT_PASSWORD, prefs.getString(Settings.VITAL_PASSWORD, ""));
		uploaderService.putExtra(ForaUploader.INTENT_BLOOD_PRESSURE_WEBLET,  prefs.getString(Settings.BLOOD_PRESSURE_WEBLET, null));
		uploaderService.putExtra(ForaUploader.INTENT_GLUCOSE_WEBLET, prefs.getString(Settings.GLUCOSE_WEBLET, null));
		
		startService(uploaderService);
		
		final Intent startStorage = new Intent(this, LegacyStorage.class);
		
		startStorage.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, allTypes);
		startStorage.putExtra(DriverInterface.INTENT_SESSION_ID, sessionHelper.getSessionId());

		startService(startStorage);
	}
	
	@Override
	protected void onStopSession() {
	
		final Intent stopForaD40Sink = new Intent(this, D40Sink.class);
		stopService(stopForaD40Sink);

		final Intent stopForaIR21Sink = new Intent(this, IR21Sink.class);
		stopService(stopForaIR21Sink);

		
		final Intent uploaderService = new Intent();
		uploaderService.setAction(ForaUploader.ACTION);
		
		stopService(uploaderService);
		
		final Intent stopStorage = new Intent(this, LegacyStorage.class);
		stopService(stopStorage);
	}
	
	@Override
	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
		final D40Sink.Discover foraD40SinkDescription = new D40Sink.Discover();
	
		allTypes = new ArrayList<ObservationType>();
		ObservationType[] types = foraD40SinkDescription.getObservationTypes(this);
		ArrayList<ObservationType> foraTypes = new ArrayList<ObservationType>();		
		
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
		driverTypes.put(foraD40SinkDescription.getDriver(), foraTypes);
		
		
		final IR21Sink.Discover foraIR21SinkDescription = new IR21Sink.Discover();
		
		types = foraIR21SinkDescription.getObservationTypes(this);
		foraTypes = new ArrayList<ObservationType>();		
		
		for(ObservationType type: types) {
			if (DriverInterface.TYPE_TEMPERATURE.equals(type.getMimeType())) {
				temperatureType = type;
			} else if (DriverInterface.TYPE_AMBIENT_TEMPERATURE.equals(type.getMimeType())) {
				ambientType = type;
			} 
			
			allTypes.add(type);
			foraTypes.add(type);			
		}
		
		driverTypes.put(foraIR21SinkDescription.getDriver(), foraTypes);
	}
	
	@Override 
	public void onBackPressed() {
		super.onBackPressed();
		
		backButtonPressed = true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		
		for(DriverConnection connection: connections) {
			try {
				if (connection.isServiceConnected()) {
					connection.unregisterClient();
				}
				unbindService(connection);
			} catch (IllegalArgumentException iae) {
				Log.d(TAG, "- ", iae);
			}
		}
		
		Log.d(TAG, "count: " + count);
		
		if (backButtonPressed && count == 0) {
			sessionHelper.destroySession();
			onStopSession();
		} if (backButtonPressed) {
			sessionHelper.stopSession();
			dbHelper.closeDatabases();
		} else {
			dbHelper.closeDatabases();
		}
		
		try {
			unregisterReceiver(pingBackReceiver);
		} catch (IllegalArgumentException iae) {
			
			Log.d(TAG, "-", iae);
		}
	}

	
	HashMap<DriverConnection, HashMap<Long, GenericObservation>> latestObservations 
		= new HashMap<DriverConnection, HashMap<Long, GenericObservation>>();
	
	HashMap<DriverConnection, Integer> connectionSinkIndex  
		= new HashMap<DriverConnection, Integer>();
	
	
	@Override
	protected void onReceiveObservations(DriverConnection connection, List<Parcelable> observations) {		
				
		SinkProperties props = null;
		
		if (connection.getDriver().getUrl().equals(D40Sink.ACTION)) {
			props = d40Sink;
		} else {
			props = ir21Sink;
		}
		
		ArrayList<ObservationType> driverTypes = this.driverTypes.get(connection.getDriver());		
		
		boolean foundAll = false;
		
		Log.d(TAG, "onReceiveObservations");
		for (Parcelable p: observations) {
			GenericObservation observation = (GenericObservation)p;
			
			long typeId = observation.getObservationTypeId();
			long time = observation.getTime();
			Log.d(TAG, "time: " + time + " type: " + typeId);
			
			if (props.latest.get(typeId) != null) {
				continue;
			}

			props.latest.put(typeId, observation);
			observationValueDao.replaceObservationValue(observation);
			
			if (props.latest.size() == driverTypes.size()) {
				foundAll = true;
				break;
			}
		}
		
		if (!foundAll) {
			
			props.index += props.chunkSize ;
			
			connection.sendMessage(
					DriverInterface.MSG_READ_SINK_OBJECTS, 
					props.index, 
					Math.min(props.size, props.index + props.chunkSize - 1));
		}
		
		refreshListView();
	}
	
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
	
		state.putInt(SIS_COUNT, count);
	}

	@Override
	public void onSensorConnectivityChanged(DriverConnection connection, int newStatus) {
		Log.d(TAG, "onSensorConnectivityChanged");
		final String prefix = newStatus == DriverConnection.SENSOR_CONNECTED 
				? "connected: " 
				: "disconnected: ";
		
		Toast.makeText(this,  prefix + connection.getDriver().getUrl(), Toast.LENGTH_LONG).show();
		
		if (newStatus == DriverConnection.SENSOR_CONNECTED) {
			connection.sendMessage(DriverInterface.MSG_READ_SINK_OBJECTS_NUM);
		}
	}
	
	@Override
	protected void onReceivedMessage(DriverConnection connection, Message msg) {
		switch(msg.what) {
		case DriverInterface.MSG_SINK_OBJECTS_NUM:
			
			Log.d(TAG, "Sink object number is " + msg.arg1);
			
			SinkProperties props = null;
			
			if (connection.getDriver().getUrl().equals(D40Sink.ACTION)) {
				props = d40Sink;
			} else {
				props = ir21Sink;
			}
			
			props.latest.clear();
			props.index = 0;
			props.size = msg.arg1;
			
			connection.sendMessage(DriverInterface.MSG_READ_SINK_OBJECTS, 0, 
				Math.min(props.size, props.chunkSize -1));
			
			break;
		}
	}
	
	SinkProperties d40Sink = new SinkProperties();
	SinkProperties ir21Sink = new SinkProperties();
	
	{
		d40Sink.chunkSize = 5;
		ir21Sink.chunkSize = 2;
	}
	
	class SinkProperties {
		final HashMap<Long, GenericObservation> latest = new HashMap<Long, GenericObservation>();
		
		int index;
		int size;
		
		int chunkSize;
	}
}
