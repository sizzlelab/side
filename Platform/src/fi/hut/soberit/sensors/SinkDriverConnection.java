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
import java.util.Collections;

import eu.mobileguild.utils.IntentFactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class SinkDriverConnection extends Handler 
	implements ServiceConnection, DriverConnection  {

	private static final int NO_COMMAND = -1;
	
	protected Messenger outgoingMessenger = null;
	
	protected Messenger incomingMessager = new Messenger(this);
	
	protected final String driverAction;
		
	private String TAG = this.getClass().getSimpleName();

	private long sessionId;

	private String clientId;
	
	private ArrayList<MessagesListener> messagesListeners = new ArrayList<MessagesListener>();
	
	int status = DriverStatusListener.UNBOUND;

	private ArrayList<DriverStatusListener> driverStatusListeners = new ArrayList<DriverStatusListener>();

	private ArrayList<Integer> executedCommands = new ArrayList<Integer>();
	
	String address;

	private boolean requestStatusOnConnect;
		
	
	public SinkDriverConnection(String driver, String clientId) {
		
		this.driverAction = driver;
		this.clientId = clientId;		
	}
	
	public void bind(Context context) {
		bind(context, false);
	}
	
	public void bind(Context context, boolean requestStatusOnConnect) {
		final Intent driverIntent = new Intent();
		driverIntent.setAction(driverAction);
				
		final boolean result = context.bindService(
				IntentFactory.create(driverAction), 
				this, 
				Context.BIND_AUTO_CREATE);
		
		Log.d(TAG, "binding to " + driverAction + " for " + clientId);
		Log.d(TAG, "result: " + result);
		
		this.requestStatusOnConnect = requestStatusOnConnect;
	}

	
	public void unbind(Context context) {		
		try {
			
			if (outgoingMessenger == null || status == DriverStatusListener.UNBOUND) {
				Log.d(TAG, "already disconnected");
				return;
			}
			
			sendMessage(SinkService.REQUEST_REGISTER_OUT_CLIENT);
			
			Log.d(TAG, "unbinding from " + driverAction);
			
			context.unbindService(this);
			
			setDriverStatus(DriverStatusListener.UNBOUND);

		} catch(IllegalArgumentException ex) {
			Log.v(TAG, "", ex);
		}
	}
	
	
	public void onServiceConnected(ComponentName className, IBinder service) {
		Log.d(TAG, "onServiceConnected.");

		outgoingMessenger = new Messenger(service);	
				
		setDriverStatus(DriverStatusListener.BOUND);
		
		sendRequestRegisterClient();
		
 		if (requestStatusOnConnect) {
 			sendRequestConnectionStatus();
 			requestStatusOnConnect = false;
 		}
 	}

	protected void sendRequestRegisterClient() {
		
		setExecutedCommand(SinkService.REQUEST_REGISTER_CLIENT);
		
		final Bundle b = new Bundle();
		b.putString(SinkService.REQUEST_FIELD_CLIENT_ID, clientId);
		b.putParcelable(SinkService.REQUEST_FIELD_REPLY_TO, incomingMessager);
		
		sendMessage(SinkService.REQUEST_REGISTER_CLIENT, 0, 0, b);
	}
	
	protected void sendMessage(int id) {
		sendMessage(id, 0, 0);
	}

	protected void sendMessage(int id, int arg1) {
		sendMessage(id, arg1, 0);
	}

	
	public synchronized void sendMessage(int id, int arg1, int arg2) {
		Log.d(TAG, "sendMessage " + id);

		sendMessage(id, arg1, arg2, null);
	}
	
	public synchronized void sendMessage(int id, int arg1, int arg2, Bundle b) {
		Log.d(TAG, "sendMessage " + id);

		if (outgoingMessenger == null) {
			throw new RuntimeException("Outgoing messenger is not connected!");
		}
		
		try {
			Message msg = Message.obtain(null, id);
			
			msg.replyTo = incomingMessager;

			msg.arg1 = arg1;
			msg.arg2 = arg2;
			
			final Bundle bundle = b == null ? new Bundle() : b;
			
			bundle.putString(SinkService.REQUEST_FIELD_CLIENT_ID, clientId);
			
			msg.setData(bundle);
			
			outgoingMessenger.send(msg);
		} catch(RemoteException re) {
			Log.v(TAG, "-", re);
		}			
	}
	
	public static int unwrapConnectionStatusMessage(Message msg) {
		switch(msg.arg1) {
		case SensorSinkService.RESPONSE_ARG1_CONNECTION_STATUS_CONNECTED:
			return DriverStatusListener.CONNECTED;

		case SensorSinkService.RESPONSE_ARG1_CONNECTION_STATUS_CONNECTING:
			return DriverStatusListener.CONNECTING;

		case SensorSinkService.RESPONSE_ARG1_CONNECTION_STATUS_DISCONNECTED:
			return DriverStatusListener.BOUND;
		default:
			throw new RuntimeException("This may be not the ConnectionStatusMessage");
		}
	}
	
	
	@Override
	public void handleMessage(Message msg) {
		Log.d(TAG, String.format("handleMessage, what = %d", msg.what));
		
		final int requestCommand = msg.what -1;
		if (msg.what > 0 && executedCommands.size() > 0 && Collections.binarySearch(executedCommands, requestCommand) == -1) {
			throw new RuntimeException(String.format(
					"Wrong response. Executed command: %s, received: %d", 
					executedCommands.toString(), msg.what));
		} else 
		if (msg.what > 0) {
			executedCommands.remove((Integer) requestCommand);
		}
				
		if (status == DriverStatusListener.UNBOUND) {
			
			Log.d(TAG, String.format("Received %d. Skipping as driver is unbound", msg.what));
			return;
		}
		
		
		switch (msg.what) {
		case SensorSinkService.RESPONSE_REGISTER_CLIENT:
		case SensorSinkService.RESPONSE_CONNECTION_STATUS:
		case SensorSinkService.BROADCAST_CONNECTION_STATUS:

			
			final Bundle data = msg.getData();
			if (data.containsKey(SensorSinkService.RESPONSE_FIELD_BT_ADDRESS)) {
				address = data.getString(SensorSinkService.RESPONSE_FIELD_BT_ADDRESS);
			}
			
			setDriverStatus(unwrapConnectionStatusMessage(msg));
			break;
			
		case SensorSinkService.RESPONSE_CONNECTION_TIMEOUT:			
			setDriverStatus(DriverStatusListener.BOUND);
			break;
			
		case SensorSinkService.RESPONSE_COUNT_OBSERVATIONS:
		case SensorSinkService.RESPONSE_READ_OBSERVATIONS:
			setDriverStatus(DriverStatusListener.CONNECTED);
			
			break;
		}	
		
		for (MessagesListener messagesListener : messagesListeners) {
			messagesListener.onReceivedMessage(this, msg);
		}
	}
	
	public void onServiceDisconnected(ComponentName className) {		
		Log.d(TAG, "onServiceDisconnected");

		outgoingMessenger = null;
		
		setDriverStatus(DriverStatusListener.UNBOUND);
	}

	protected void setDriverStatus(int newStatus) {

		final int oldStatus = status;
		status = newStatus;
		
		for (DriverStatusListener listener: driverStatusListeners) {
			listener.onDriverStatusChanged(this, oldStatus, newStatus);
		}
	}

	@Deprecated
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
		Log.d(TAG, "REQUEST_COUNT_OBSERVATIONS");
		
		setExecutedCommand(SensorSinkService.REQUEST_COUNT_OBSERVATIONS);
		setDriverStatus(DriverStatusListener.COUNTING);
		
		sendMessage(SensorSinkService.REQUEST_COUNT_OBSERVATIONS);
	}
	
	protected void setExecutedCommand(int command) {
		if (status == DriverStatusListener.UNBOUND) {
			throw new RuntimeException("Driver is unbound");
		}
		
		if (status != DriverStatusListener.CONNECTED && (
				command == SensorSinkService.REQUEST_COUNT_OBSERVATIONS ||
				command == SensorSinkService.REQUEST_READ_OBSERVATIONS)) {
			throw new RuntimeException("Driver is not connected");
		}
		
		executedCommands.add(command);
	}

	public void sendReadObservations(long[] types, int start, int end) {
		Log.d(TAG, "REQUEST_READ_OBSERVATIONS");
		
		setExecutedCommand(SensorSinkService.REQUEST_READ_OBSERVATIONS);
		setDriverStatus(DriverStatusListener.DOWNLOADING);
		
		final Bundle bundle = new Bundle();
		
		final TypeFilter filter = new TypeFilter();
		
		for(long type : types) {
			filter.add(type);
		}
		
		bundle.putParcelable(SensorSinkService.REQUEST_FIELD_DATA_TYPES, filter);
		
		sendMessage(SensorSinkService.REQUEST_READ_OBSERVATIONS, start, end, bundle);
	}

	
	public void addMessagesListener(MessagesListener messagesListener) {
		messagesListeners.add(messagesListener);
	}
	
	public void removeMessagesListener(MessagesListener listener) {
		messagesListeners.remove(listener);
	}

	@Override
	public void sendStartConnecting(String address) {
		Log.d(TAG, "REQUEST_START_CONNECTING " + address);
		
		this.address = address;
		
		final Bundle b = new Bundle();
		b.putString(SensorSinkService.REQUEST_FIELD_BT_ADDRESS, address);
		
		sendMessage(SensorSinkService.REQUEST_START_CONNECTING, 0, 0, b);
	}
	
	public void sendStartConnecting(String address, int timeout) {
		Log.d(TAG, "REQUEST_START_CONNECTING " + address + " timeout: " + timeout);
		
		this.address = address;
		
		final Bundle b = new Bundle();
		b.putString(SensorSinkService.REQUEST_FIELD_BT_ADDRESS, address);
		b.putInt(SensorSinkService.REQUEST_FIELD_TIMEOUT, timeout);
		
		sendMessage(SensorSinkService.REQUEST_START_CONNECTING, 0, 0, b);
	}

	
	public void sendRequestConnectionStatus() {
		Log.d(TAG, "REQUEST_CONNECTION_STATUS");
				
		setExecutedCommand(SensorSinkService.REQUEST_CONNECTION_STATUS);
		
		sendMessage(SensorSinkService.REQUEST_CONNECTION_STATUS, 0);
	}	
	
	public void addDriverStatusListener(DriverStatusListener driverStatusListener) {
		driverStatusListeners.add(driverStatusListener);
	}
	
	public void removeDriverStatusListener(DriverStatusListener driverStatusListener) {
		driverStatusListeners.remove(driverStatusListener);
	}


	public int getDriverStatus() {
		return status;
	}
	
	public boolean isConnectedToSensor() {
		return 
				status != DriverStatusListener.UNBOUND &&
				status != DriverStatusListener.BOUND &&
				status != DriverStatusListener.CONNECTING;
				
	}
	
	public void sendDisconnectRequest() {
		Log.d(TAG, "REQUEST_DISCONNECT");
		
		setExecutedCommand(NO_COMMAND);
		setDriverStatus(DriverStatusListener.BOUND);
		
		sendMessage(SensorSinkService.REQUEST_DISCONNECT, 0);
	}	
	
	public String getDeviceAddress() {
		return address;
	}
}
