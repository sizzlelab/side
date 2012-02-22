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

import fi.hut.soberit.sensors.generic.GenericObservation;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public abstract class SensorSinkService extends SinkService {

	public static final int RESPONSE_REGISTER_CLIENT = REQUEST_REGISTER_CLIENT + 1;
			
	
    public static final int REQUEST_CONNECTION_STATUS = 100;
    
    public static final int RESPONSE_CONNECTION_STATUS = 101;

    public static final int RESPONSE_ARG1_CONNECTION_STATUS_CONNECTING = 0;
    
    public static final int RESPONSE_ARG1_CONNECTION_STATUS_CONNECTED = 1;
    
    public static final int RESPONSE_ARG1_CONNECTION_STATUS_DISCONNECTED = 2;

    public static final int BROADCAST_CONNECTION_STATUS = -102;

    
	
	public static final int REQUEST_START_CONNECTING = 104;
	
	public static final String REQUEST_FIELD_BT_ADDRESS = "bt address";

		
	public static final int REQUEST_STOP_CONNECTING = 106;
	

	public static final int REQUEST_COUNT_OBSERVATIONS = 120;
	
	public static final int RESPONSE_COUNT_OBSERVATIONS = 121;


	public static final int REQUEST_READ_OBSERVATIONS = 122;

	public static final int RESPONSE_READ_OBSERVATIONS = 123;
	
    public static final String REQUEST_FIELD_DATA_TYPES = "data types";

    public static final String RESPONSE_FIELD_OBSERVATIONS = "observations";
	
	
	Thread connectivityThread;
	
	public void setConnectivityThread(ConnectivityThread connectivityThread) {
		
		this.connectivityThread = (Thread) connectivityThread;
	}
	
	protected void onRegisterClient(final String clientId) {
		
		sendConnectionStatus(clientId, RESPONSE_REGISTER_CLIENT);
	}

	protected void sendConnectionStatus(final String clientId, int responseCode) {
		final int arg1 = connectivityThread != null && connectivityThread.isAlive()
				? 
						(((ConnectivityThread) connectivityThread).isConnected() 
								? RESPONSE_ARG1_CONNECTION_STATUS_CONNECTED
								: RESPONSE_ARG1_CONNECTION_STATUS_CONNECTING )
				: RESPONSE_ARG1_CONNECTION_STATUS_DISCONNECTED;

		send(clientId, responseCode, arg1);
	}
	
	protected void broadcastConnectionStatus(int arg1) {
		
		synchronized(clients) {		
			for (String clientId : clients.keySet()) {
				send(clientId, BROADCAST_CONNECTION_STATUS, arg1);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		if (connectivityThread != null && connectivityThread.isAlive()) {
			Log.d(TAG, "interrupting");
			connectivityThread.interrupt();
			((ConnectivityThread) connectivityThread).closeSocket();
		}
	}
	
	protected void onReceivedMessage(Message msg, String clientId) {
		
		final Bundle bundle = msg.getData();
		bundle.setClassLoader(getClassLoader());
		
		switch(msg.what) {
		case REQUEST_START_CONNECTING:
			
			final String address = bundle.getString(REQUEST_FIELD_BT_ADDRESS);
			
			((ConnectivityThread) connectivityThread).setBluetoothAddress(address);
			connectivityThread.start();	
			break;
			
		case REQUEST_STOP_CONNECTING:
			
			if (connectivityThread != null && connectivityThread.isAlive()) {
				Log.d(TAG, "interrupting");
				connectivityThread.interrupt();
				((ConnectivityThread) connectivityThread).closeSocket();
			}
	
			connectivityThread = null;
			
			break;
			
		case REQUEST_CONNECTION_STATUS:
			
			sendConnectionStatus(clientId, RESPONSE_CONNECTION_STATUS);
			break;				
		}
	}

	protected Thread getConnectivityThread() {
		return connectivityThread;
	}
	
	public void returnObservation(String clientId, ArrayList<GenericObservation> observations) {
		
		final Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(RESPONSE_FIELD_OBSERVATIONS, observations);
		
		final Message msg = Message.obtain(null, RESPONSE_READ_OBSERVATIONS);
		msg.what = RESPONSE_READ_OBSERVATIONS;
		msg.setData(bundle);
		
		send(clientId, true, msg);
	}
}
