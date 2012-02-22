/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import fi.hut.soberit.sensors.generic.ObservationType;

public abstract class DriverConnectionImpl extends Handler 
	implements ServiceConnection, DriverConnection  {

	int sensorConnectivityStatus = -1 /* SensorSinkActivityListener.SENSOR_DISCONNECTED */;
	 
	protected Messenger outgoingMessenger = null;
	
	protected Messenger incomingMessager = new Messenger(this);
	
	protected final Driver driver;
	
	protected final HashMap<Long, ObservationType> typesMap = new HashMap<Long, ObservationType>();
	
	private String TAG = this.getClass().getSimpleName();

	private List<ObservationType> types;

	private boolean setDataTypes;

	private long sessionId;

	private ArrayList<SensorSinkActivityListener> sensorConnectivityListeners = new ArrayList<SensorSinkActivityListener>();

	private String clientId;
	
	// TODO: setDataTypes parameter does not make sense. 
	// It should be architecturally decided how do we support two services with same name and
	// one service with multiple clients. 
	public DriverConnectionImpl(Driver driver, List<ObservationType> types, boolean setDataTypes, String clientId) {
		
		this.driver = driver;
		this.types = types;
		this.setDataTypes = setDataTypes;
		this.clientId = clientId;
		
		for(ObservationType typeShort : types) {
			typesMap.put(typeShort.getId(), typeShort);
			Log.d(TAG, "Type id " + typeShort.getId());
		}
	}
	
	/**
	 * Connect to service, which has been already started.
	 * 
	 */	
	public void bind(Context context) {
		Log.d(TAG, "bind");

		final Intent driverIntent = new Intent();
		driverIntent.setAction(driver.getUrl());			
		
		Log.d(TAG, "binding to " + driver.getUrl());
		Log.d(TAG, "result: " + context.bindService(driverIntent, this, Context.BIND_DEBUG_UNBIND));
	}
	
	public void unbind(Context context) {
		try {
			context.unbindService(this);
		} catch (IllegalArgumentException iae) {
			Log.d(TAG, "- ", iae);
		}
	}
	
	public synchronized void unregisterClient() {
		Log.d(TAG, "unregisterClient");

		sendMessage(DriverInterface.REQUEST_UNREGISTER_CLIENT);
	}

	public void onServiceConnected(ComponentName className, IBinder service) {
		Log.d(TAG, "onServiceConnected.");
		outgoingMessenger = new Messenger(service);

		registerClient();
	}

	// We keep this method synchronised as it uses two send messages
	private synchronized void registerClient() {
		Log.d(TAG, "registerClient");

		sendMessage(DriverInterface.REQUEST_REGISTER_CLIENT);
		
		if (!setDataTypes) {
			return;
		}
		
		final Bundle conf = new Bundle();
		conf.putParcelableArrayList(
				DriverInterface.MSG_FIELD_DATA_TYPES, 
				new ArrayList<ObservationType>(types));
					
		conf.putLong(DriverInterface.MSG_REGISTER_SESSION_ID, sessionId);
		
		sendMessage(DriverInterface.REQUEST_REGISTER_CLIENT);
	}
	
	public void sendMessage(int id) {
		sendMessage(id, 0, 0);
	}

	public void sendMessage(int id, int arg1) {
		sendMessage(id, arg1, 0);
	}

	
	public synchronized void sendMessage(int id, int arg1, int arg2) {
		Log.d(TAG, "sendMessage " + id);

		sendMessage(id, arg1, arg2, null);
	}
	
	public synchronized void sendMessage(int id, int arg1, int arg2, Bundle b) {
		Log.d(TAG, "sendMessage " + id);

		if (outgoingMessenger == null) {
			return;
		}
		
		try {
			Message msg = Message.obtain(null, id);
			
			msg.replyTo = incomingMessager;

			msg.arg1 = arg1;
			msg.arg2 = arg2;
			
			final Bundle bundle = b == null ? new Bundle() : b;
			
			bundle.putString(DriverInterface.MSG_FIELD_CLIENT_ID, clientId);
			
			msg.setData(b);
			
			outgoingMessenger.send(msg);
		} catch(RemoteException re) {
			Log.v(TAG, "-", re);
		}			
	}
	
	private void setSensorConnectivityStatus(int status) {
		Log.d(TAG, "Sensor connectivity: " + status + ", " + driver);
		
		if (sensorConnectivityStatus == status) {
			return;
		}
		
		this.sensorConnectivityStatus = status;
		
		for(SensorSinkActivityListener listener: sensorConnectivityListeners) {
			listener.onSensorSinkStatusChanged(this, status);
		}
	}

	public void addSensorStatusListener(SensorSinkActivityListener listener) {
		sensorConnectivityListeners.add(listener);
	}

	@Override
	public void handleMessage(Message msg) {
		
//		switch (msg.what) {
//		case DriverInterface.RESPONSE_OBSERVATIONS:
//			
//			final Bundle bundle = msg.getData();
//			bundle.setClassLoader(this.getClass().getClassLoader());
//
//			Log.d(TAG, String.format("Received observations"));				 
//
//			final List<Parcelable> observations = (List<Parcelable>) bundle.getParcelableArrayList(DriverInterface.MSG_FIELD_OBSERVATIONS);
//			Log.d(TAG, String.format("Received '%d' observations", driver.getId()));				 
//		
//			onReceiveObservations(observations);
//			
//			break;
//		
//		case DriverInterface.RESPONSE_SENSOR_CONNECTED:
//			// setSensorConnectivityStatus(SensorSinkActivityListener.SENSOR_CONNECTED);
//			break;
//
//		case DriverInterface.RESPONSE_SENSOR_DISCONNECTED:
//			//setSensorConnectivityStatus(SensorSinkActivityListener.SENSOR_DISCONNECTED);
//			break;
//			
//		default:
//			onReceivedMessage(msg);
//		}
	}
	
	protected void onReceivedMessage(Message msg) {
		
	}

	public abstract void onReceiveObservations(List<Parcelable> observations);

	public void onServiceDisconnected(ComponentName className) {		
		Log.d(TAG, "onServiceDisconnected");

		outgoingMessenger = null;
	}

	public boolean isConnected() {
		return outgoingMessenger != null ;
	}
	
	public void setServiceMessenger(Messenger serviceMessenger) {
		this.outgoingMessenger = serviceMessenger;
	}

	public Messenger getServiceMessenger() {
		return outgoingMessenger;
	}
	
	@Override
	public String toString() {
		return "DriverConnection [driver=" 
			+ driver 
			+ ", isConnected=" 
			+ isConnected() 
			+ "]";
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	
	public Driver getDriver() {
		return driver;
	}
	
	public String getDriverAction() {
		return driver.getUrl();
	}	
	
	public void connect(String address) {
		throw new RuntimeException("Didn't implement this!");
	}
}
