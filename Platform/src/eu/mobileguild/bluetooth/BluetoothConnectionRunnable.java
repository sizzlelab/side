package eu.mobileguild.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import eu.mobileguild.utils.ThreadUtil;

public abstract class BluetoothConnectionRunnable implements Runnable {

	public final String TAG =  this.getClass().getSimpleName();
	
	public static final int CONNECT = 0;
	public static final int READ = 1;
	
	public final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	
	protected String address;
	protected int timeout;
	
	protected int state = CONNECT;
	protected BluetoothSocket socket;
	protected BluetoothAdapter adapter;
	private int delayBeforeReconnect;


	private boolean stopFlag;
	
	public BluetoothConnectionRunnable(String address, int timeout) {
		
		this.address = address;
		this.timeout = timeout;
		
		delayBeforeReconnect = timeout / 5;
		
		// has to be created inside thread with a handler
		adapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	@Override
	public void run() {
		Log.d(TAG, "SynchronisationRunnable::run");
		
		long timeConnecting = 0;
		long lastConnectionAttempt = System.currentTimeMillis(); 
			
		try {
			Log.d(TAG, String.format("%d, %d", timeConnecting, timeout));
			/**
			 *  prevent reconnection -- additional measure,
			 *  taken because one of the functions in connect() clears the interrupt flag 
			 */
			while(timeConnecting < timeout && !stopFlag) {
				ThreadUtil.throwIfInterruped();

				switch(state) {
				case CONNECT: 
					if (!connect()) {
						final long now = System.currentTimeMillis();
						timeConnecting += now - lastConnectionAttempt;
						lastConnectionAttempt = now;
						Thread.sleep(delayBeforeReconnect);
						Log.d(TAG, "connection failed");
						break;
					}
					Log.d(TAG, "connected successfully");
					timeConnecting = 0;
					state = READ;
					break;
				
				case READ: 
					read();
					break;
				}
			}
		} catch (InterruptedException e) {
			Log.d(TAG, "exception", e);
		} catch (IOException e) {
			Log.d(TAG, "exception", e);
		} finally {
			Log.d(TAG, "finally");
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean connect() {
        Log.d(TAG, "SynchronisationRunnable::connect");

		try {

			/**
			 * One of functions below seem to clear interrupted flag :'((((
			 * Please, prove me wrong
			 */
			BluetoothDevice device = null;

			device = adapter.getRemoteDevice(address);

            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            
            socket.connect();                
            onConnect();
            
            Log.d(TAG, "Succeess connecting");

            return true;
		} catch(IOException e) {
			return false;
		} 
	}

	protected abstract void onConnect() throws IOException;
	
	protected abstract void read() throws IOException, InterruptedException;

	public boolean isStopFlag() {
		return stopFlag;
	}

	public void setStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}
}
