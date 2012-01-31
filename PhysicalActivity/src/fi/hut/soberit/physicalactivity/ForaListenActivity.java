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
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ListView;
import eu.mobileguild.ui.MultidimensionalArrayAdapter;
import eu.mobileguild.utils.LittleEndian;
import fi.hut.soberit.fora.D40Driver;
import fi.hut.soberit.physicalactivity.legacy.LegacyStorage;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationValueDao;
import fi.hut.soberit.sensors.activities.BroadcastListenerActivity;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;

public class ForaListenActivity extends BroadcastListenerActivity  {
	
	private static final String TAG = ForaListenActivity.class.getSimpleName();

	private static final String SIS_COUNT = "count";

	protected static final long SLEEP = 0;

	private MultidimensionalArrayAdapter listAdapter;
	private ObservationType pulseType;
	private ObservationType glucoseType;
	private ObservationType bloodPressureType;


	private ObservationValueDao observationValueDao;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

	private ArrayList<String[]> observations;

	private boolean backButtonPressed;

	private int count = 0;

	
    @Override
    public void onCreate(Bundle sis) {

    	settingsFileName = Settings.APP_PREFERENCES_FILE;
    	sessionIdPreference = Settings.VITAL_SESSION_IN_PROCESS;
    	startNewSession = true;
    	registerInDatabase = true;
    	sessionName = getString(R.string.session_name_vital);
    	
        super.onCreate(sis);
        setContentView(R.layout.fora_listen);
        		
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

		if (getIntent() == null) {
			count = sis.getInt(SIS_COUNT);
		} else {
			count = observations.size();
		} 
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
		}		
		
		return item;
	}

	@Override
	protected void onStartSession() {		

		final SharedPreferences prefs = getSharedPreferences(
				settingsFileName, 
				MODE_PRIVATE);
	
		final Intent foraDriverIntent = new Intent();
		foraDriverIntent.setAction(D40Driver.ACTION);
		foraDriverIntent.putExtra(D40Driver.INTENT_DEVICE_ADDRESS, prefs.getString(Settings.FORA_BLUETOOTH_ADDRESS, null));
		foraDriverIntent.putExtra(D40Driver.INTENT_BROADCAST_FREQUENCY, 0l);
		
		startService(foraDriverIntent);			
						
		final Intent startStorage = new Intent(this, LegacyStorage.class);
		
		startStorage.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, allTypes);
		startStorage.putExtra(DriverInterface.INTENT_SESSION_ID, sessionId);

		startService(startStorage);
	}
	
	protected void stopSession() {
		super.stopSession();
	
		if (registerInDatabase) {
			sessionDao.updateSession(sessionId, System.currentTimeMillis());
		}
		
		
		final Intent stopForaDriver = new Intent(this, D40Driver.class);
		stopService(stopForaDriver);
				
		final Intent stopStorage = new Intent(this, LegacyStorage.class);
		stopService(stopStorage);
	}
	
	@Override
	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
		final D40Driver.Discover foraDriverDescription = new D40Driver.Discover();

	
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

		if (backButtonPressed) {
			stopSession();
		}
		
		final int newCount = observations.size();
		Log.d(TAG, "newCount: " + count);

		if (backButtonPressed) {
			new Thread(new Runnable() {
				public void run() {
					
					final String name = String.format("%s (%d)", 
							getString(R.string.session_name_vital),
							newCount - count);
					
					
					
					while(true) {
						try {
							if (newCount > count && sessionDao.updateSession(sessionId, name) > 0) {
								return;
							} else if (newCount == count && sessionDao.delete(sessionId) > 0) {
								return;
							}
						} catch(SQLiteException e) {
							
						}
							
						try {
							Thread.currentThread().sleep(SLEEP);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}
				}
			}).start();
			
			dbHelper.closeDatabases();

		}
		
		try {
			unregisterReceiver(pingBackReceiver);
		} catch (IllegalArgumentException iae) {
			
			Log.d(TAG, "-", iae);
		}
	}

	@Override
	protected void onReceiveObservations(List<Parcelable> observations) {
				
		Log.d(TAG, "onReceiveObservations");
		for (Parcelable p: observations) {
			GenericObservation observation = (GenericObservation)p;
			
			Log.d(TAG, "time: " + observation.getTime() + " type: " + observation.getObservationTypeId());
			
			
			final long res = observationValueDao.insertObservationValue(observation);			
		}
		
	
		
		refreshListView();
		
	}
	
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
	
		state.putInt(SIS_COUNT, count);
	}
}
