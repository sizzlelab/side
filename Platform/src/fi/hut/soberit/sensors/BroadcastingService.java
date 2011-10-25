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
import java.util.HashMap;
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

public abstract class BroadcastingService extends Service {
	
	public static String STARTED_PREFIX = ".STARTED";

	public final String TAG = this.getClass().getSimpleName();
	
	public static final String INTENT_BROADCAST_FREQUENCY = "broadcast_frequency";

	public static final long DEFAULT_BROADCAST_FREQUENCY = 1000;

	private ArrayList<Messenger> clients = new ArrayList<Messenger>();

	private Vector<GenericObservation> observations = new Vector<GenericObservation>();
	
	protected HashMap<String, ObservationType> typesMap = new HashMap<String, ObservationType>();
	
    private final Messenger messenger = new Messenger(new IncomingHandler());

	private long broadcastFrequency = 0;

	private long lastObservationBroadcasted = 0;
	
	private BroadcastReceiver broadcastControlReceiver = new BroadcastControlReceiver();

	private IntentFilter broadcastControlMessageFilter;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent == null) {
			return START_REDELIVER_INTENT;
		}

		setBroadcastFrequency(intent.getLongExtra(INTENT_BROADCAST_FREQUENCY, DEFAULT_BROADCAST_FREQUENCY));
		Log.d(TAG, "broadcastFrequency: " + broadcastFrequency);				
		
		broadcastControlMessageFilter = new IntentFilter();
		broadcastControlMessageFilter.addAction(DriverInterface.ACTION_SESSION_STOP);
		
		broadcastControlReceiver = new BroadcastControlReceiver();
		registerReceiver(broadcastControlReceiver, broadcastControlMessageFilter);		
		
		final Intent pingBack = new Intent();
		pingBack.setAction(getDriverAction() + STARTED_PREFIX);
		Log.d(TAG, "sending ping back " + pingBack.getAction());
		sendBroadcast(pingBack);	
		
		return START_REDELIVER_INTENT;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");		
		
		return messenger.getBinder();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		
		unregisterReceiver(broadcastControlReceiver);		
	}
	
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DriverInterface.MSG_REGISTER_CLIENT:
				Log.d(TAG, "MSG_REGISTER_CLIENT");
				
				clients.add(msg.replyTo);
				
				onRegisterClient();
				break;
			
			case DriverInterface.MSG_REGISTER_DATA_TYPES:
				Log.d(TAG, "MSG_REGISTER_DATA_TYPES");

				final Bundle payload = msg.getData();
				payload.setClassLoader(getClassLoader());
				
				final ArrayList<Parcelable> parcelables = payload.getParcelableArrayList(DriverInterface.MSG_FIELD_DATA_TYPES);
				
				typesMap.clear();
				
				for(Parcelable obj : parcelables) {
					final ObservationType type = (ObservationType)obj;
					typesMap.put(type.getMimeType(), type);
				}
					
				onRegisterDataTypes();				
				break;
				
			case DriverInterface.MSG_UNREGISTER_CLIENT:
				Log.d(TAG, "MSG_UNREGISTER_CLIENT");

				clients.remove(msg.replyTo);
				
				Log.d(TAG, "Clients left: " + clients.size());
				onUnregisterClient();
				
				break;
				
			default:
				super.handleMessage(msg);
			}
		}
	}

	protected void onRegisterClient() {
		
	}
	
	protected void onRegisterDataTypes() {
		
	}

	protected void onUnregisterClient() {
	}
	
	protected void broadcastObservation() {
		final long now = System.currentTimeMillis();

		if (now - lastObservationBroadcasted <= broadcastFrequency) {
			return;
		}
		Log.d(TAG, "onSensorChanged broadcasted");

		lastObservationBroadcasted  = now;
		
		final Bundle bundle = new Bundle();
		ArrayList<GenericObservation> copy;
		synchronized(observations) {
			Log.d(TAG, "observations: " + observations.size());
			copy = new ArrayList<GenericObservation>();
			for(int i = 0; i<observations.size(); i++) {
				copy.add(i, observations.get(i));
			}

			Collections.copy(copy, observations);
			observations.clear();
		}
		
		bundle.putParcelableArrayList(DriverInterface.MSG_FIELD_OBSERVATIONS, copy);

		Log.d(TAG, "clients: " + clients.size());
		
		for (int i = clients.size() - 1; i >= 0; i--) {
			try {
				final Message msg = Message.obtain(null,
						DriverInterface.MSG_OBSERVATION);

				msg.setData(bundle);

				clients.get(i).send(msg);
			} catch (RemoteException e) {
				clients.remove(i);
			}
		}
	}

	public long getBroadcastFrequency() {
		return broadcastFrequency;
	}

	public void setBroadcastFrequency(long broadcastFrequency) {
		this.broadcastFrequency = broadcastFrequency;
	}
	
	protected void addObservation(GenericObservation observation) {
		synchronized(observations) {
			observations.add(observation);
		}
		
		broadcastObservation();
	}


	public static abstract class Discover extends BroadcastReceiver {
			
		public static final String TAG = 
			BroadcastingService.class.getSimpleName() + " " + Discover.class.getSimpleName();
		
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
		BroadcastingService.this.stopSelf();
	}
	
	public abstract String getDriverAction();
}
