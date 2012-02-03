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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;

public abstract class SinkService extends Service {

	public final String TAG = this.getClass().getSimpleName();
	
	public static String STARTED_PREFIX = ".STARTED";
	
	private HashMap<String, Messenger> clients = new HashMap<String, Messenger>();
			
    private final Messenger messenger = new Messenger(new IncomingHandler());

	private BroadcastReceiver broadcastControlReceiver = new BroadcastControlReceiver();

	private IntentFilter broadcastControlMessageFilter;

	public HashMap<String, ArrayDeque<Message>> messageQueues = new HashMap<String, ArrayDeque<Message>>();
	
	Thread connectivityThread;
	
	public static final String INTENT_DEVICE_ADDRESS = "device_address";
	
	public void setConnectivityThread(ConnectivityThread connectivityThread) {
		
		this.connectivityThread = (Thread) connectivityThread;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent == null) {
			return START_REDELIVER_INTENT;
		}

		broadcastControlMessageFilter = new IntentFilter();
		broadcastControlMessageFilter.addAction(DriverInterface.ACTION_SESSION_STOP);
		
		broadcastControlReceiver = new BroadcastControlReceiver();
		registerReceiver(broadcastControlReceiver, broadcastControlMessageFilter);		
		
		final Intent pingBack = new Intent();
		pingBack.setAction(getDriverAction() + STARTED_PREFIX);
		Log.d(TAG, "sending ping back " + pingBack.getAction());
		sendBroadcast(pingBack);	
		
		final String address = intent.getStringExtra(INTENT_DEVICE_ADDRESS);
		Log.d(TAG, "address: " + address);
		
		((ConnectivityThread) connectivityThread).setBluetoothAddress(address);
		connectivityThread.start();	
		
		return START_REDELIVER_INTENT;
    }

	@Override
	public IBinder onBind(Intent intent) {
				
		return messenger.getBinder();
	}


	private void registerClient(final String clientId, final Messenger replyTo) {
		synchronized(clients) {					
			clients.put(clientId, replyTo);
			
			final ArrayDeque<Message> waitingMessages = messageQueues.get(clientId);
			
			if (waitingMessages == null) {
				messageQueues.put(clientId, new ArrayDeque<Message>());
			} else if (waitingMessages.size() > 0){
				for (Message waitingMsg: waitingMessages) {
					send(clientId, waitingMsg);
				}
			}
			onRegisterClient();
			
			onRegisterDataTypes();

		}	
		
		final int response = 
				connectivityThread != null && 
				connectivityThread.isAlive() && 
				((ConnectivityThread) connectivityThread).isConnected() 
			? DriverInterface.MSG_SENSOR_CONNECTED
			: DriverInterface.MSG_SENSOR_DISCONNECTED;

		send(clientId, response, 0);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);

		final Bundle bundle = intent.getExtras();
		bundle.setClassLoader(getClassLoader());
		final String clientId = bundle.getString(DriverInterface.MSG_FIELD_CLIENT_ID);
		
		Log.d(TAG, "onUnbind " + clientId);
		
		if (clientId == null) {
			throw new RuntimeException("Client must supply an id");
		}
		
		/**
		 * We synchronize on clients in order to defend against situations, 
		 * where messages are being sent, at the same time 
		 * as clients being unregistered
		 */
		synchronized(clients) {
			
			clients.remove(clientId);
			
			Log.d(TAG, "Clients left: " + clients.size());
			onUnregisterClient();
		}				
		
		
		return false;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");

		if (connectivityThread != null && connectivityThread.isAlive()) {
			Log.d(TAG, "interrupting");
			connectivityThread.interrupt();
			((ConnectivityThread) connectivityThread).closeSocket();
		}
		
		
		try {
			unregisterReceiver(broadcastControlReceiver);
		} catch(IllegalArgumentException iae) {
			Log.d(TAG, "illegal argument", iae);
		}
	}
	
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			
			final Bundle bundle = msg.getData();
			bundle.setClassLoader(getClassLoader());
			final String clientId = bundle.getString(DriverInterface.MSG_FIELD_CLIENT_ID);
			
			if (clientId == null) {
				throw new RuntimeException("Client must supply an id; in message " + msg.what);
			}
			
			
			if (msg.what == DriverInterface.MSG_REGISTER_CLIENT) {
				final Messenger replyTo = (Messenger) bundle.get(DriverInterface.MSG_FIELD_REPLY_TO);		
	
				registerClient(clientId, replyTo);	
				return;
			}


			onReceivedMessage(msg, clientId);
		}
	}

	protected void onRegisterClient() {
		
	}
	
	protected void onRegisterDataTypes() {
		
	}

	protected void onUnregisterClient() {
	}
	
	protected void onReceivedMessage(Message msg, String clientId) {
		
	}
	
	public void returnObservation(String clientId, ArrayList<GenericObservation> observations) {
		
		final Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(DriverInterface.MSG_FIELD_OBSERVATIONS, observations);
		
		final Message msg = Message.obtain(null, DriverInterface.MSG_OBSERVATION);
		msg.what = DriverInterface.MSG_OBSERVATION;
		msg.setData(bundle);
		
		send(clientId, msg);
	}
	
	public void send(String clientId, Message msg) {
		Log.v(TAG, String.format("send %d to %s", msg.what, clientId));
		
		
		synchronized(clients) {
			final Messenger messanger = clients.get(clientId); 
			
			try {
				if (messanger != null) {
					messanger.send(msg);
					return;
				}
			} catch (RemoteException re) {
				Log.d(TAG, "shouldn't happen:", re);
				messageQueues.get(clientId).add(msg);
				return;
			}
			
			messageQueues.get(clientId).add(msg);
		}
	}
	
	
	public void send(String clientId, int what, int arg1) {
		
		final Message msg = Message.obtain(null, what);
		
		msg.arg1 = arg1;
		
		send(clientId, msg);
	}
	
	public void broadcast(int what) {
		
		for (String clientId : clients.keySet()) {
			send(clientId, what, 0);
		}
	}
	

	public static abstract class Discover extends BroadcastReceiver {
			
		public static final String TAG = 
			SinkService.class.getSimpleName() + " " + Discover.class.getSimpleName();
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive " + intent.getAction());

			if (!DriverInterface.ACTION_START_DISCOVERY.equals(intent.getAction())) {
				return;
			}
			
			final ObservationType [] types = getObservationTypes(context);
			for(ObservationType type: types) {
				final Intent response = new Intent();
				response.setAction(DriverInterface.ACTION_DISCOVERED);
				
				response.putExtra(DriverInterface.INTENT_FIELD_DATA_TYPE, (Parcelable)type);	
				context.sendBroadcast(response);
			}		
		}
		
		public abstract ObservationType[] getObservationTypes(Context context);
		
		public abstract Driver getDriver();
	}
	
	public class BroadcastControlReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive " + intent.getAction());
			
			if (!DriverInterface.ACTION_SESSION_STOP.equals(intent.getAction())) {
				return;
			}

			onStopSession();
		}
	}

	protected void onStopSession() {
		SinkService.this.stopSelf();
	}
	
	public abstract String getDriverAction();
	
	
	public static class QueueKey implements Comparable<QueueKey>{
		
		private String clientId;
		private long typeId;

		public QueueKey(String clientId, long typeId) {
			this.clientId = clientId;
			this.typeId = typeId;
		}

		
		public boolean equals(Object that) {
			if (!(that instanceof QueueKey)) {
				return false;
			}
			
			QueueKey another = (QueueKey)that;
			
			return clientId.equals(another.clientId) && typeId == another.typeId;
		}
		
		@Override
		public int compareTo(QueueKey another) {
			
			int res = clientId.compareTo(another.clientId); 
			if (res != 0) {
				return res;
			}
			
			return (int) (typeId - another.typeId);
		}
	}
		
	public ConnectivityThread getConnectivityThread() {
		return (ConnectivityThread) connectivityThread;
	}
}
