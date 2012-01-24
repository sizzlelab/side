package fi.hut.soberit.sensors.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import fi.hut.soberit.sensors.BindOnDriverStartStrategy;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.SensorStatusListener;
import fi.hut.soberit.sensors.SinkDriverConnection;
import fi.hut.soberit.sensors.generic.ObservationType;

public abstract class SinkListenerFragmentActivity extends FragmentActivity implements SensorStatusListener {
	
	protected final String TAG = this.getClass().getSimpleName();

	protected ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();
	
	protected Handler refreshHandler = new Handler();
	
	protected Runnable refreshRunnable;

	protected BroadcastReceiver pingBackReceiver;

	protected ArrayList<ObservationType> allTypes = new ArrayList<ObservationType>();
	
	protected HashMap<Driver, ArrayList<ObservationType>> driverTypes = new HashMap<Driver, ArrayList<ObservationType>>();
			
	protected SessionHelper sessionHelper;

	protected boolean registerInDatabase;

	protected boolean startNewSession = false;

	protected DatabaseHelper dbHelper;

	protected String clientId = this.getClass().getName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		if (startNewSession) {
			sessionHelper = new SessionHelper(this, dbHelper);
			sessionHelper.setRegisterInDatabase(registerInDatabase);
		}
						
		buildDriverAndUploadersTree(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (sessionHelper == null || sessionHelper.hasStarted()) {
			bindToOngoingSession();
			return;
		}
		
		startSession();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	
		try {
			unregisterReceiver(pingBackReceiver);
		} catch(IllegalArgumentException e) {
			
		}
		
		refreshHandler.removeCallbacks(refreshRunnable);
		
		for(DriverConnection connection: connections) {
			connection.unbind(this);
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
					clientId);
			driverConnection.setSessionId(sessionHelper == null ? -1 : sessionHelper.getSessionId());
			driverConnection.addSensorStatusListener(this);
			
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
					clientId);
			driverConnection.setSessionId(sessionHelper.getSessionId());
			driverConnection.addSensorStatusListener(this);
			
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
	
	
	protected abstract void onReceiveObservations(DriverConnectionImpl connection, List<Parcelable> observations);

	public abstract void onSensorStatusChanged(DriverConnectionImpl connection, int newStatus);

	public class DriverConnectionImpl extends SinkDriverConnection {

		public DriverConnectionImpl(Driver driver, String clientId) {
			super(driver, clientId);
		}
		
		public void onReceiveObservations(List<Parcelable> observations) {
			SinkListenerFragmentActivity.this.onReceiveObservations(this, observations);

			sessionHelper.updateSession();
		}		

		// TODO: Implement "addListener" pattern
		@Override
		protected void onReceivedMessage(Message msg) {
			SinkListenerFragmentActivity.this.onReceivedMessage(this, msg);
		}
	}


	protected void onReceivedMessage(DriverConnectionImpl connection, Message msg) {
		
	}
	
	public Driver findDriverByTypeId(long typeId) {
		for (Driver driver: driverTypes.keySet()) {
			for (ObservationType type: driverTypes.get(driver)) {
				if (type.getId() != typeId) {
					continue;
				}
				
				return driver;
			}
		}
		
		return null;
	}
}
