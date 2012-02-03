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
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import fi.hut.soberit.sensors.generic.ObservationType;

public class SinkDriverConnection extends Handler 
	implements ServiceConnection, DriverConnection  {

	int sensorConnectivityStatus = SensorStatusListener.SENSOR_DISCONNECTED;
	
	protected Messenger outgoingMessenger = null;
	
	protected Messenger incomingMessager = new Messenger(this);
	
	protected final String driverAction;
		
	private String TAG = this.getClass().getSimpleName();

	private long sessionId;

	private ArrayList<SensorStatusListener> sensorConnectivityListeners = new ArrayList<SensorStatusListener>();

	private String clientId;
	
	private MessagesListener messagesListener;
	
	private ObservationsListener observationsListener;
	
	public SinkDriverConnection(String driver, String clientId) {
		
		this.driverAction = driver;
		this.clientId = clientId;		
	}
	
	/**
	 * Connect to service, which has been already started.
	 * 
	 */	
	public void bind(Context context) {
		Log.d(TAG, "bind");

		final Intent driverIntent = new Intent();
		driverIntent.setAction(driverAction);
		
		driverIntent.putExtra(DriverInterface.MSG_FIELD_REPLY_TO, incomingMessager);
		driverIntent.putExtra(DriverInterface.MSG_FIELD_CLIENT_ID, clientId);
		
		Log.d(TAG, "binding to " + driverAction);
		Log.d(TAG, "result: " + context.bindService(driverIntent, this, Context.BIND_AUTO_CREATE));
	}
	
	public void unbind(Context context) {		
		try {
			
			if (outgoingMessenger == null) {
				Log.d(TAG, "already disconnected");
				return;
			}
			
			Log.d(TAG, "unbinding from " + driverAction);
			
			context.unbindService(this);
		} catch(IllegalArgumentException ex) {
			Log.v(TAG, "", ex);
		}
	}
	
	
	public void onServiceConnected(ComponentName className, IBinder service) {
		Log.d(TAG, "onServiceConnected.");

		outgoingMessenger = new Messenger(service);	
		
		final Bundle b = new Bundle();
		b.putParcelable(DriverInterface.MSG_FIELD_REPLY_TO, incomingMessager);
		
		sendMessage(DriverInterface.MSG_REGISTER_CLIENT, 0, 0, b);
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
			
			msg.setData(bundle);
			
			outgoingMessenger.send(msg);
		} catch(RemoteException re) {
			Log.v(TAG, "-", re);
		}			
	}
	
	private void setSensorConnectivityStatus(int status) {
		Log.d(TAG, "Sensor connectivity: " + status + ", " + driverAction);
		
		if (sensorConnectivityStatus == status) {
			return;
		}
		
		this.sensorConnectivityStatus = status;
		
		for(SensorStatusListener listener: sensorConnectivityListeners) {
			listener.onSensorStatusChanged(this, status);
		}
	}

	public void addSensorStatusListener(SensorStatusListener listener) {
		sensorConnectivityListeners.add(listener);
	}

	@Override
	public void handleMessage(Message msg) {
		
		switch (msg.what) {
		case DriverInterface.MSG_OBSERVATION:
			
			final Bundle bundle = msg.getData();
			bundle.setClassLoader(this.getClass().getClassLoader());

			Log.d(TAG, String.format("Received observations"));				 

			final List<Parcelable> observations = (List<Parcelable>) bundle.getParcelableArrayList(DriverInterface.MSG_FIELD_OBSERVATIONS);
			Log.d(TAG, String.format("Received observations from " + driverAction));				 
		
			if (observationsListener != null) {
				this.observationsListener.onReceiveObservations(this, observations);
			}
			
			break;
		
		case DriverInterface.MSG_SENSOR_CONNECTED:
			setSensorConnectivityStatus(SensorStatusListener.SENSOR_CONNECTED);
			break;

		case DriverInterface.MSG_SENSOR_DISCONNECTED:
			setSensorConnectivityStatus(SensorStatusListener.SENSOR_DISCONNECTED);
			break;
		case DriverInterface.MSG_SENSOR_CONNECTIVITY:
			break;
			
		default:
			if (messagesListener != null) {
				messagesListener.onReceivedMessage(this, msg);
			}
		}
	}
	
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
			+ driverAction 
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
		return null;
	}
	
	public String getDriverAction() {
		return driverAction;
	}
	
	public void sendReadObservationNumberMessage() {
		sendMessage(DriverInterface.MSG_READ_SINK_OBJECTS_NUM);
	}
	
	public void sendReadObservations(ArrayList<Long> types, int start, int end) {
		final Bundle bundle = new Bundle();
		
		final TypeFilter filter = new TypeFilter();
		filter.addAll(types);
		
		bundle.putParcelable(DriverInterface.MSG_FIELD_DATA_TYPES, filter);
		
		sendMessage(DriverInterface.MSG_READ_SINK_OBJECTS, start, end, bundle);
	}

	public MessagesListener getMessagesListener() {
		return messagesListener;
	}

	public void setMessagesListener(MessagesListener messagesListener) {
		this.messagesListener = messagesListener;
	}

	public ObservationsListener getObservationsListener() {
		return observationsListener;
	}

	public void setObservationsListener(ObservationsListener observationsListener) {
		this.observationsListener = observationsListener;
	}
}
