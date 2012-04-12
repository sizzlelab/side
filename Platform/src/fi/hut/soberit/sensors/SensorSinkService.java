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

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import eu.mobileguild.utils.BundleFactory;
import eu.mobileguild.utils.ThreadUtil;
import fi.hut.soberit.sensors.generic.GenericObservation;

public abstract class SensorSinkService extends SinkService {

	public static final int RESPONSE_REGISTER_CLIENT = REQUEST_REGISTER_CLIENT + 1;
			
	
    public static final int REQUEST_CONNECTION_STATUS = 100;
    
    public static final int RESPONSE_CONNECTION_STATUS = 101;

    public static final int RESPONSE_ARG1_CONNECTION_STATUS_CONNECTING = 1;
    
    public static final int RESPONSE_ARG1_CONNECTION_STATUS_CONNECTED = 2;
    
    public static final int RESPONSE_ARG1_CONNECTION_STATUS_DISCONNECTED = 3;

    public static final int BROADCAST_CONNECTION_STATUS = -102;

	
	public static final int REQUEST_START_CONNECTING = 104;
	
	public static final String REQUEST_FIELD_BT_ADDRESS = "bt address";

		
	public static final int REQUEST_DISCONNECT = 106;
	

	public static final int REQUEST_COUNT_OBSERVATIONS = 120;
	
	public static final int RESPONSE_COUNT_OBSERVATIONS = 121;


	public static final int REQUEST_READ_OBSERVATIONS = 122;

	public static final int RESPONSE_READ_OBSERVATIONS = 123;
	
    public static final int RESPONSE_CONNECTION_TIMEOUT = 124;
    
    
    public static final int REQUEST_CHANGE_ADDRESS = 130;
    
	
    public static final String RESPONSE_FIELD_BT_ADDRESS = "bt address";

	public static final String REQUEST_FIELD_TIMEOUT = "timeout";

    
    public static final String REQUEST_FIELD_DATA_TYPES = "data types";

    public static final String RESPONSE_FIELD_OBSERVATIONS = "observations";

	
	ExecutorService executor;
	
	// created a separate class to use it's object as a monitor. 
	protected ConnectionDetails connection = new ConnectionDetails();
	
	public class SensorExecutorService extends ThreadPoolExecutor {
		
		public SensorExecutorService() {
			super(1, 3, 5000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(20));
		}
		
		public void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			if (t == null && r instanceof Future) {
				try {
					Object result = ((Future) r).get();
				} catch (CancellationException ce) {
			       t = ce;
				} catch (ExecutionException ee) {
			       t = ee.getCause();
				} catch (InterruptedException ie) {
			       Thread.currentThread().interrupt(); // ignore/reset
				}
			}  
			if (t != null) {
				Log.d(TAG, "exception ", t);
				shutdownNow();
				
				new DisconnectTask(connection, null, NO_ORGIN_MSG_ID).run();
				
				executor = new SensorExecutorService();
			}
		}
	}
	
	
	public class ConnectTask implements Runnable {

		public final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

		private ConnectionDetails connection;
		private BluetoothAdapter adapter;

		private int timeout;
		
		private long replyToMsgId;
		
		/* long delay in between connections */
		private int timeToSleep;

		public static final int NO_TIMEOUT = -1;
		public static final int NO_SLEEP = -1;
		
		/* small delay in between connections */
		public static final int NAP = 300; 
		
		public ConnectTask(ConnectionDetails connection, long replyToMsgId, int timeout, int timeToSleep) {
			this.connection = connection;
			this.timeout = timeout;
			this.timeToSleep = timeToSleep;
			this.replyToMsgId = replyToMsgId;
			
			adapter = BluetoothAdapter.getDefaultAdapter();
		}
		
		public void setBluetoothAddress(String address) {
			this.connection.address = address;
		}
		
		public String getBluetoothAddress() {
			return connection.address;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "ConnectTask::run");
			
			long timeoutTime = System.currentTimeMillis() + timeout;
			
			connection.status = ConnectionDetails.CONNECTING;
			
			try {
				while(timeout == NO_TIMEOUT || timeoutTime > System.currentTimeMillis()) {
					ThreadUtil.throwIfInterruped();

					/**
					 *  prevent reconnection -- additional measure,
					 *  taken because one of the functions in connect() clears the interrupt flag 
					 */
					synchronized(connection) {
						if (connection.stopConnecting) {
							Log.d(TAG, "stopped connecting using the 'stopConnecting' flag!");
							connection.stopConnecting = false;
							// we set connected to false in order for client classes to observe isConnected status right 
							connection.status = ConnectionDetails.DISCONNECTED;

							return;
						}
					}
					
					if (connect()) {
						Log.d(TAG, "connected successfully");
						
						onConnect(replyToMsgId);
						return;
					}
					
					final int time = timeToSleep == NO_SLEEP ? NAP : timeToSleep;
					
					Log.v(TAG, "sleeping for " + time);
					Thread.sleep(time);
				}
				
				
				onTimeout(replyToMsgId);
				connection.stopConnecting = false;
 
				connection.status = ConnectionDetails.DISCONNECTED;
				return;

			} catch (Exception re) {
				Log.d(TAG, "exception", re);
				
				connection.stopConnecting = false;
				connection.status = ConnectionDetails.DISCONNECTED;
				connection.socket = null;
			} finally {
				Log.d(TAG, "finally");
				

			}
		}
		
		private boolean connect() throws InterruptedException {
	        Log.d(TAG, "ConnectTask::connect");

			try {

				/**
				 * One of functions below seem to clear interrupted flag :'((((
				 * Please, prove me wrong
				 */
				BluetoothDevice device = adapter.getRemoteDevice(connection.address);

	            final BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
	            socket.connect();
	            
	            synchronized(connection) {
	            	connection.socket = socket;            	
	            	connection.status = ConnectionDetails.CONNECTED;
	            }
	            
	            return true;
			} catch(IOException ioe) {
				Log.d(TAG, "", ioe);
				
				return false;
			} 
		}
	}
	
	protected void onConnect(long replyToMsgId) throws IOException, InterruptedException {
		Log.d(TAG, "onConnect");
		
		final Bundle b = new Bundle();
		b.putLong(RESPONSE_FIELD_REPLY_TO_MSG_ID, replyToMsgId);
		
		send(connection.orginator, 
				SensorSinkService.RESPONSE_CONNECTION_STATUS, 
				SensorSinkService.RESPONSE_ARG1_CONNECTION_STATUS_CONNECTED);
	}
	
	protected void onTimeout(long replyToMsgId) {
		Log.d(TAG, "onTimeout");
		
		final Bundle b = new Bundle();
		b.putLong(RESPONSE_FIELD_REPLY_TO_MSG_ID, replyToMsgId);
		
		send(connection.orginator, RESPONSE_CONNECTION_TIMEOUT, 0, b);
	}
	
	protected void onDisconnect(long replyToMsgId) {
		Log.d(TAG, "onDisconnect");
		
		
	}
	
	
	class DisconnectTask implements Runnable {
		
		private ConnectionDetails connectionDetails;
		private String clientId;
		private long replyToMsgId;

		public DisconnectTask(ConnectionDetails info, String clientId, long replyToMsgId) {
			connectionDetails = info;
			this.clientId = clientId;
			this.replyToMsgId = replyToMsgId;
		}
		
		public void run() {
	        Log.d(TAG, "DisconnectTask::run");
			
			synchronized(connection) {
				try {
					onDisconnect(replyToMsgId);
					
					connectionDetails.stopConnecting = connectionDetails.status == ConnectionDetails.CONNECTING;
					connectionDetails.status = ConnectionDetails.DISCONNECTED;
					connectionDetails.orginator = null;
					connectionDetails.socket.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (clientId == null) {
				return;
			}
			
			sendConnectionStatus(clientId, RESPONSE_ARG1_CONNECTION_STATUS_DISCONNECTED, replyToMsgId);
		}	
	}

	class ChangeDeviceTask implements Runnable {
		private ConnectionDetails connection;
		private String clientId;
		private int timeout;
		private int timeToSleep;
		private String address;
		private long replyToMsgId;

		public ChangeDeviceTask(ConnectionDetails info, String clientId, long replyToMsgId, String address, int timeout, int timeToSleep) {
			connection = info;
			this.timeout = timeout;
			this.timeToSleep = timeToSleep;
			this.clientId = clientId;
			this.address = address;
			this.replyToMsgId = replyToMsgId;
		}
		
		public void run() {
		
			new DisconnectTask(connection, clientId, replyToMsgId).run();
			
			connection.status = ConnectionDetails.CONNECTING;
			connection.orginator = clientId;
			connection.address = address;
			
			new ConnectTask(connection, replyToMsgId, timeout, timeToSleep).run();
		}
	}
	
	
	public SensorSinkService() {
		
		executor = new SensorExecutorService();
	}
		
	@Override
	protected void onRegisterClient(final String clientId, long msgId) {
		
		sendConnectionStatus(clientId, RESPONSE_REGISTER_CLIENT, msgId);
	}

	protected void sendConnectionStatus(final String clientId, int responseCode, long msgId) {		
		Bundle b = new Bundle();
		
		final int arg1 = connectionStatusToResponseArg1(connection.status);
		
		if (connection.status != ConnectionDetails.DISCONNECTED) {
						
			b.putString(RESPONSE_FIELD_BT_ADDRESS, connection.address);
		}
		
		b.putLong(RESPONSE_FIELD_REPLY_TO_MSG_ID, msgId);
						
		send(clientId, responseCode, arg1, b);
	}
	
	private int connectionStatusToResponseArg1(int status) {
		switch(status) {
		case ConnectionDetails.DISCONNECTED : return RESPONSE_ARG1_CONNECTION_STATUS_DISCONNECTED;
		case ConnectionDetails.CONNECTING : return RESPONSE_ARG1_CONNECTION_STATUS_CONNECTING;
		case ConnectionDetails.CONNECTED : return RESPONSE_ARG1_CONNECTION_STATUS_CONNECTED;
		
		}
		throw new RuntimeException("Shouldn't happen");
	}

	protected void broadcastConnectionStatus(int arg1, long replyToMsgId) {
		
		final Bundle b = new Bundle();
		b.putLong(RESPONSE_FIELD_REPLY_TO_MSG_ID, replyToMsgId);
		
		synchronized(clients) {
			for (String clientId : clients.keySet()) {
				send(clientId, BROADCAST_CONNECTION_STATUS, arg1, b);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		if (connection.status == ConnectionDetails.CONNECTED) {
			new DisconnectTask(connection, (String) null, NO_ORGIN_MSG_ID).run();
		}
		
		executor.shutdownNow();
		
	}
	
	@Override
	protected void onReceivedMessage(Message msg, String clientId, long msgId) {
		
		final Bundle bundle = msg.getData();
		bundle.setClassLoader(getClassLoader());
				
		switch(msg.what) {
		case REQUEST_START_CONNECTING:
		{
			
			if (connection.status != ConnectionDetails.DISCONNECTED) {
				throw new RuntimeException("Shouldn't happen");
			}

			connection.address = bundle.getString(REQUEST_FIELD_BT_ADDRESS);
			
			final int timeout = bundle.getInt(REQUEST_FIELD_TIMEOUT, ConnectTask.NO_TIMEOUT);
			executor.submit(new ConnectTask(connection, msgId, timeout, 2000));
			
			connection.status = ConnectionDetails.CONNECTING;
			connection.orginator = clientId;
			break;
		}
		case REQUEST_DISCONNECT:
		{			
			executor.submit(new DisconnectTask(
					connection, 
					clientId,
					msgId));

			break;
		}	
		case REQUEST_CHANGE_ADDRESS:
		{			
			final String address = bundle.getString(REQUEST_FIELD_BT_ADDRESS);

			final int timeout = bundle.getInt(REQUEST_FIELD_TIMEOUT, ConnectTask.NO_TIMEOUT);

			executor.submit(new ChangeDeviceTask(
					connection, 
					clientId,
					msgId,
					address, 
					timeout, 2000));
			
			break;
		}	
		case REQUEST_CONNECTION_STATUS:
			
			sendConnectionStatus(clientId, RESPONSE_CONNECTION_STATUS, msgId);
			break;				
		}
	}	
	
	public void addTask(Callable task) {
		executor.submit(task);
	}
	
	public ConnectionDetails getConnection() {
		return connection;
	}
}
