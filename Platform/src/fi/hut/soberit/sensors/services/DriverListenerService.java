package fi.hut.soberit.sensors.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.ObservationType;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

public abstract class DriverListenerService extends Service {

	public final String TAG = this.getClass().getSimpleName();
	
	protected ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();

	protected HashMap<String, ObservationType> types = new HashMap<String, ObservationType>();
	
	protected int onStartFlag = Service.START_FLAG_REDELIVERY;
	
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		if (intent == null) {
			return Service.START_FLAG_REDELIVERY;
		}
		
		ArrayList<Parcelable> parcelableTypes = null;
		
		Bundle payload = intent.getExtras();
		payload.setClassLoader(getClassLoader());
				
		parcelableTypes = payload.getParcelableArrayList(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES);
			
		HashMap<Driver, ArrayList<ObservationType>> drivers = new HashMap<Driver, ArrayList<ObservationType>>();
		
		for(Parcelable parcelable: parcelableTypes) {
			final ObservationType type = (ObservationType) parcelable;
						
			final String mimeType = type.getMimeType();
			if (!isMimeTypeInteresting(mimeType)) {
				continue;
			}
			
			Log.d(TAG, "listening to " + type);

			
			this.types.put(mimeType, type);
			
			ArrayList<ObservationType> driverTypes = drivers.get(type.getDriver());
			
			if (driverTypes == null) {
				driverTypes = new ArrayList<ObservationType>();
				drivers.put(type.getDriver(), driverTypes);
			}
			
			driverTypes.add(type);
		}
		
		for (Driver driver : drivers.keySet()) {			
			final DriverConnectionImpl driverConnection = new DriverConnectionImpl(driver, drivers.get(driver));
			connections.add(driverConnection);	
			
			driverConnection.bind(this);
		}
		
		return Service.START_FLAG_REDELIVERY;
	}
	
	protected boolean isMimeTypeInteresting(String mimeType) {
		return true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		for(DriverConnection connection : connections) {
			if (!connection.isServiceConnected()) {
				continue;
			}
			
			connection.unregisterClient();
			unbindService(connection);
		}
	}
	
	class DriverConnectionImpl extends DriverConnection {

		public DriverConnectionImpl(Driver driver, List<ObservationType> types) {
			super(driver, types, false);
		}

		@Override
		public void onReceiveObservations(List<Parcelable> observations) {
			DriverListenerService.this.onReceiveObservations(observations);
		}
	}
	
	public abstract void onReceiveObservations(List<Parcelable> observations);
}
