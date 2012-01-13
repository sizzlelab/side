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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import fi.hut.soberit.sensors.BindOnDriverStartStrategy;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.SensorConnectivitityListener;
import fi.hut.soberit.sensors.generic.ObservationType;

public abstract class SinkListenerActivity extends Activity implements SensorConnectivitityListener {
	
	protected final String TAG = this.getClass().getSimpleName();

	protected ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();
	
	protected Handler refreshHandler = new Handler();
	
	protected Runnable refreshRunnable;

	protected BroadcastReceiver pingBackReceiver;

	protected ArrayList<ObservationType> allTypes = new ArrayList<ObservationType>();
	
	protected HashMap<Driver, ArrayList<ObservationType>> driverTypes = new HashMap<Driver, ArrayList<ObservationType>>();
			
	protected SessionHelper sessionHelper;

	protected boolean registerInDatabase;

	protected boolean startNewSession;

	protected DatabaseHelper dbHelper;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		sessionHelper = startNewSession ? new SessionHelper(this, dbHelper) : null;
		sessionHelper.setRegisterInDatabase(registerInDatabase);
						
		buildDriverAndUploadersTree(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (sessionHelper == null || sessionHelper.hasStarted()) {
			bindToOngoingSession();
			return;
		}
		
		startSession();
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
	
	
	/** 
	 * Method has to initialise four methods: allTypes, driverTypes
	 * @param savedInstanceState
	 */
	protected abstract void buildDriverAndUploadersTree(Bundle savedInstanceState); 
	
	protected void bindToOngoingSession() {
		Log.d(TAG, "bindToOngoingSession");
		
		for(Driver driver: driverTypes.keySet()) {
			final DriverConnectionImpl driverConnection = new DriverConnectionImpl(
					driver, 
					driverTypes.get(driver), 
					true);
			driverConnection.setSessionId(sessionHelper.getSessionId());
			driverConnection.addSensorConnectivityListener(this);
			
			connections.add(driverConnection);
			final Intent driverIntent = new Intent();
			driverIntent.setAction(driver.getUrl());			
			
			Log.d(TAG, "binding to " + driver.getUrl());
			Log.d(TAG, "result: " + bindService(driverIntent, driverConnection, Context.BIND_DEBUG_UNBIND));		
		}
		
		onResumeSession();
	}
	
	protected void onResumeSession() {}
		
	protected final void startSession() {
		Log.d(TAG, "startSession");
		
		sessionHelper.startSession();
		
		pingBackReceiver = new BindOnDriverStartStrategy(connections);
		final IntentFilter pingBackFilter = new IntentFilter();

		for(Driver driver: driverTypes.keySet()) {
			final DriverConnectionImpl driverConnection = new DriverConnectionImpl(
					driver, 
					driverTypes.get(driver), 
					true);
			driverConnection.setSessionId(sessionHelper.getSessionId());
			driverConnection.addSensorConnectivityListener(this);
			
			connections.add(driverConnection);
			
			final String action = driver.getUrl() + BroadcastingService.STARTED_PREFIX;
			pingBackFilter.addAction(action);
			Log.d(TAG, "Registered pingBackReceiver for " + action);
		}
		registerReceiver(pingBackReceiver, pingBackFilter);
		
		onStartSession();	
	}

	protected void onStartSession() {} 	
	
	protected final void stopSession() {
		Log.d(TAG, "stopSession");
				
		sessionHelper.stopSession();
	
		onStopSession();
	}
	
	protected void onStopSession() { }
	
	
	protected abstract void onReceiveObservations(DriverConnection driver, List<Parcelable> observations);

	public abstract void onSensorConnectivityChanged(DriverConnection driver, int newStatus);

	
	public class DriverConnectionImpl extends DriverConnection {

		public DriverConnectionImpl(Driver driver, List<ObservationType> types, boolean startServices) {
			super(driver, types, startServices);
		}
		
		public void onReceiveObservations(List<Parcelable> observations) {
			SinkListenerActivity.this.onReceiveObservations(this, observations);

			sessionHelper.updateSession();
		}		

		// TODO: Implement observer pattern for messages
		@Override
		protected void onReceivedMessage(Message msg) {
			SinkListenerActivity.this.onReceivedMessage(this, msg);
		}
	}


	protected void onReceivedMessage(DriverConnection connection, Message msg) {
		
	}
}
