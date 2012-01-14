/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import fi.hut.soberit.sensors.BindOnDriverStartStrategy;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.UploadedTypeDao;
import fi.hut.soberit.sensors.UploaderDao;
import fi.hut.soberit.sensors.core.storage.StorageDao;
import fi.hut.soberit.sensors.core.storagetype.StorageTypeDao;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.generic.StorageType;
import fi.hut.soberit.sensors.generic.UploadedType;
import fi.hut.soberit.sensors.generic.Uploader;
import fi.hut.soberit.sensors.storage.GenericObservationStorage;

public abstract class BroadcastListenerActivity extends Activity {
	
	private static final String INTENT_SESSION_ID = "session id";

	private static final String SIS_SESSION_ID = INTENT_SESSION_ID;

	protected final String TAG = this.getClass().getSimpleName();
	
	protected SessionDao sessionDao;

	protected DatabaseHelper dbHelper;
		
	protected long sessionId;

	protected ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();
	
	protected Handler refreshHandler = new Handler();
	
	protected Runnable refreshRunnable;

	protected BroadcastReceiver pingBackReceiver;

	protected ArrayList<Uploader> uploaders;
	
	protected ArrayList<ObservationType> allTypes;
	
	protected String settingsFileName;

	protected String sessionIdPreference;

	public long lastSessionUpdate;
	
	protected static boolean startNewSession = false;

	protected static boolean registerInDatabase = true;
	
	private ArrayList<Storage> storages;

	private Storage defaultStorage;

	protected HashMap<Driver, ArrayList<ObservationType>> driverTypes;

	protected String sessionName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		sessionDao = startNewSession && registerInDatabase ? new SessionDao(dbHelper) : null;
		
		if (!startNewSession) {
			final Intent intent = getIntent();
			if (intent == null) {
				sessionId = savedInstanceState.getLong(SIS_SESSION_ID, -1);
			} else {
				sessionId = intent.getLongExtra(INTENT_SESSION_ID, -1);
			}
		}
		
		buildDriverAndUploadersTree(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (!startNewSession) {
			bindToOngoingSession();
			return;
		}
		
		final SharedPreferences prefs = getSharedPreferences(
				settingsFileName, 
				MODE_PRIVATE);
		
		sessionId = prefs.getLong(sessionIdPreference, -1);
		if (sessionId != -1) {
			
			bindToOngoingSession();
			return;
		}
		
		startSession();
	}
	
	@Override
	protected void onSaveInstanceState (Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putLong(SIS_SESSION_ID, sessionId);		
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
	
		try {
			unregisterReceiver(pingBackReceiver);
		} catch(IllegalArgumentException e) {
			
		}
		
		refreshHandler.removeCallbacks(refreshRunnable);
		
		for(DriverConnection connection: connections) {
			if (connection.isServiceConnected()) {
				connection.unregisterClient();
			}
			
			try {
				unbindService(connection);
			} catch(IllegalArgumentException ex) {
				Log.d(TAG, "", ex);
			}
		}		
	}
	
	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
		Log.d(TAG, "buildDriverAndUploadersTree");
		ObservationTypeDao observationTypeDao = new ObservationTypeDao(dbHelper);
		ObservationKeynameDao observationKeynameDao = new ObservationKeynameDao(dbHelper);
		
		DriverDao driverDao = new DriverDao(dbHelper);
		
		allTypes = new ArrayList<ObservationType>(
				observationTypeDao.getObservationTypes(null, true));
								
		driverTypes = new HashMap<Driver, ArrayList<ObservationType>>();
		
		for(ObservationType type: allTypes) {
			type.setKeynames(observationKeynameDao.getKeynames(type.getId()));
			
			final Driver driver = driverDao.getDriverFromType(type.getId());
			type.setDriver(driver);
			
			ArrayList<ObservationType> types = driverTypes.get(driver);
			
			if (types == null) {
				types = new ArrayList<ObservationType>();
				driverTypes.put(driver, types);
			}
			
			types.add(type);
		}		
		
		UploaderDao uploaderDao = new UploaderDao(dbHelper);
		UploadedTypeDao uploadedTypeDao  = new UploadedTypeDao(dbHelper);
        
		uploaders = uploaderDao.getUploaders(Boolean.TRUE);
		
		for(Uploader uploader: uploaders) {
			final List<UploadedType> list = uploadedTypeDao.getTypes(uploader.getId()); 
			
			final UploadedType[] uploadedTypes = new UploadedType[list.size()];
			uploader.setUploadedTypes(list.toArray(uploadedTypes));
		}
		
		final StorageDao storageDao = new StorageDao(dbHelper);
		final StorageTypeDao storageTypeDao = new StorageTypeDao(dbHelper);
		
		final List<Storage> allStorages = storageDao.get();
		
		for(Storage storage: allStorages) {
						
			storage.setTypes(new ArrayList<ObservationType>());
			for(StorageType storageType: storageTypeDao.get(storage.getId())) {
				
				searchingEnabledTypes:
				for(ObservationType type: allTypes) {
					if (type.getId() == storageType.getObservationTypeId()) {
						storage.getTypes().add(type);
						break searchingEnabledTypes;
					}
				}
			}
		}
		
		defaultStorage = null;
		
		storages = new ArrayList<Storage>();
		for(Storage storage: allStorages) {
			if (storage.getTypes().size() == 0) {
				continue;
			}
			
			if (storage.getUrl().equals(GenericObservationStorage.ACTION)) {
				defaultStorage = storage;
			} else {
				storages.add(storage);
			}
		}
	}
		
	protected void startSession() {
		Log.d(TAG, "startSession");
		
		final SharedPreferences prefs = getSharedPreferences(
				settingsFileName, 
				MODE_PRIVATE);

		sessionId = registerInDatabase 
			? sessionDao.insertSession(sessionName, System.currentTimeMillis())
			: 1;

		final Editor editor = prefs.edit();
		editor.putLong(sessionIdPreference, sessionId);
		editor.commit();
		
		pingBackReceiver = new BindOnDriverStartStrategy(connections);
		final IntentFilter pingBackFilter = new IntentFilter();

		for(Driver driver: driverTypes.keySet()) {
			final DriverConnectionImpl driverConnection = new DriverConnectionImpl(
					driver, 
					driverTypes.get(driver), 
					true);
			driverConnection.setSessionId(sessionId);
			
			connections.add(driverConnection);
			
			final String action = driver.getUrl() + BroadcastingService.STARTED_PREFIX;
			pingBackFilter.addAction(action);
			Log.d(TAG, "Registered pingBackReceiver for " + action);
		}
		registerReceiver(pingBackReceiver, pingBackFilter);
		
		onStartSession();	
	}

	protected void onStartSession() {

		final Intent sessionStartedBroadcast = new Intent();
		sessionStartedBroadcast.setAction(DriverInterface.ACTION_SESSION_STARTED);
		sessionStartedBroadcast.putExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, allTypes);
		sessionStartedBroadcast.putExtra(DriverInterface.INTENT_FIELD_UPLOADERS, uploaders);
		sessionStartedBroadcast.putExtra(DriverInterface.INTENT_FIELD_STORAGES, storages);
		sessionStartedBroadcast.putExtra(DriverInterface.INTENT_SESSION_ID, sessionId);

		sendBroadcast(sessionStartedBroadcast);

		if (defaultStorage == null) {
			return;
		}
		final Intent storageService = new Intent();
		storageService.setAction(defaultStorage.getUrl());
	
		storageService.putExtra(
				DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, 
				(ArrayList)defaultStorage.getTypes());
		storageService.putExtra(DriverInterface.INTENT_SESSION_ID, sessionId);

		startService(storageService);
	}
	
	protected void stopSession() {
		Log.d(TAG, "stopSession");
		
		if (registerInDatabase) {
			sessionDao.updateSession(sessionId, System.currentTimeMillis());
		}
		
		final SharedPreferences prefs = getSharedPreferences(
				settingsFileName, 
				MODE_PRIVATE);
		
		final Editor editor = prefs.edit();
		editor.remove(sessionIdPreference);
		editor.commit();
		
		onStopSession();		
	}
	
	protected void onStopSession() {

		final Intent sessionStartedBroadcast = new Intent();
		sessionStartedBroadcast.setAction(DriverInterface.ACTION_SESSION_STOP);
		
		sendBroadcast(sessionStartedBroadcast);
		
		if (defaultStorage == null) {
			return;
		}
		
		final Intent defaultStorageService = new Intent();
		defaultStorageService.setAction(defaultStorage.getUrl());
		
		stopService(defaultStorageService);
	}

	protected void bindToOngoingSession() {
		Log.d(TAG, "bindToOngoingSession");
		
		for(Driver driver: driverTypes.keySet()) {
			final DriverConnectionImpl driverConnection = new DriverConnectionImpl(
					driver, 
					driverTypes.get(driver), 
					true);
			driverConnection.setSessionId(sessionId);
			
			connections.add(driverConnection);
			final Intent driverIntent = new Intent();
			driverIntent.setAction(driver.getUrl());			
			
			Log.d(TAG, "binding to " + driver.getUrl());
			Log.d(TAG, "result: " + bindService(driverIntent, driverConnection, Context.BIND_DEBUG_UNBIND));		
		}
		
		onResumeSession();
	}
	
	protected void onResumeSession() {

		
	}

	protected abstract void onReceiveObservations(List<Parcelable> observations);

	
	public class DriverConnectionImpl extends DriverConnection {

		public DriverConnectionImpl(Driver driver, List<ObservationType> types, boolean startServices) {
			super(driver, types, startServices);
		}

		public void onReceiveObservations(List<Parcelable> observations) {
			BroadcastListenerActivity.this.onReceiveObservations(observations);

			
			if (!startNewSession) {
				return;
			}
			
			final long now = System.currentTimeMillis();
			
			if (now - lastSessionUpdate   < 60*1000) {
				return;
			}
			
			lastSessionUpdate = now;
			
			if (registerInDatabase) {
				sessionDao.updateSession(sessionId, now);
			}
		}		
	}
}
