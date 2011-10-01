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

public abstract class DriverConnection extends Handler implements ServiceConnection  {

	protected Messenger outgoingMessenger = null;
	
	protected Messenger incomingMessager = new Messenger(this);
	
	protected final Driver driver;
	
	protected final HashMap<Long, ObservationType> typesMap = new HashMap<Long, ObservationType>();

	private String TAG = this.getClass().getSimpleName();

	private List<ObservationType> types;

	private boolean setDataTypes;

	private long sessionId;
	
	// TODO: setDataTypes parameter does not make sense. 
	// It should be architecturally decided how do we support two services with same name and
	// one service with multiple clients. 
	public DriverConnection(Driver driver, List<ObservationType> types, boolean setDataTypes) {
		
		this.driver = driver;
		this.types = types;
		this.setDataTypes = setDataTypes;
		
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
	
	public void unregisterClient() {
		Log.d(TAG, "unregisterClient");

		try {
			Message msg = Message.obtain(null,
					DriverInterface.MSG_UNREGISTER_CLIENT);
			msg.replyTo = incomingMessager;
			outgoingMessenger.send(msg);
		} catch (RemoteException e) {
			Log.d(TAG, "No worries, service has crashed. No need to do anything: ", e);
		}
	}

	public void onServiceConnected(ComponentName className, IBinder service) {
		Log.d(TAG, "onServiceConnected.");
		outgoingMessenger = new Messenger(service);

		registerClient();
	}

	private void registerClient() {
		Log.d(TAG, "registerClient");

		try {
			Message msg = Message.obtain(null,
					DriverInterface.MSG_REGISTER_CLIENT);
			
			msg.replyTo = incomingMessager;
			outgoingMessenger.send(msg);
			
			if (!setDataTypes) {
				return;
			}
			
			msg = Message.obtain(null,
					DriverInterface.MSG_REGISTER_DATA_TYPES);
			
			final Bundle conf = new Bundle();
			conf.putParcelableArrayList(
					DriverInterface.MSG_FIELD_DATA_TYPES, 
					new ArrayList<ObservationType>(types));
			
			conf.putLong(DriverInterface.MSG_REGISTER_SESSION_ID, sessionId);
			
			msg.setData(conf);
			outgoingMessenger.send(msg);			
			
		} catch (RemoteException e) {

		}
	}

	@Override
	public void handleMessage(Message msg) {
		
		switch (msg.what) {
		case DriverInterface.MSG_OBSERVATION:
			
			final Bundle bundle = msg.getData();
			bundle.setClassLoader(this.getClass().getClassLoader());

			Log.d(TAG, String.format("Received observations"));				 

			final List<Parcelable> observations = (List<Parcelable>) bundle.getParcelableArrayList(DriverInterface.MSG_FIELD_OBSERVATIONS);
			Log.d(TAG, String.format("Received '%d' observations", driver.getId()));				 
		
			onReceiveObservations(observations);
			
			break;
			
		default:
			super.handleMessage(msg);
		}
	}
	
	public abstract void onReceiveObservations(List<Parcelable> observations);

	public void onServiceDisconnected(ComponentName className) {		
		Log.d(TAG, "onServiceDisconnected");

		outgoingMessenger = null;
	}

	public boolean isServiceConnected() {
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
			+ isServiceConnected() 
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
}
