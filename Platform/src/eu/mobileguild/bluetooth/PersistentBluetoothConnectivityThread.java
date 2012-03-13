/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;
import eu.mobileguild.utils.ThreadUtil;
import fi.hut.soberit.sensors.ConnectivityThread;
import fi.hut.soberit.sensors.DriverInterface;

public abstract class PersistentBluetoothConnectivityThread extends Thread implements ConnectivityThread {

	public final String TAG =  this.getClass().getSimpleName();
	
	boolean connected = false;
	
	public final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	
	protected String address;
	protected int timeout;
	
	protected BluetoothSocket socket;
	protected BluetoothAdapter adapter;

	private int sleepShortly;

	private int sleepLong;

	private boolean stopped = false;
	
	public PersistentBluetoothConnectivityThread(int timeout, int sleepShortly, int sleepLong) {
		this.timeout = timeout;
		
		this.sleepShortly = sleepShortly;
		this.sleepLong = sleepLong;
				
		// has to be created inside thread with a handler
		adapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public void setBluetoothAddress(String address) {
		this.address = address;
	}
	
	public String getBluetoothAddress() {
		return address;
	}

	
	@Override
	public void run() {
		Log.d(TAG, "PersistentBluetoothConnectivityThread::run");
		
		long timeConnecting = 0;
		long lastConnectionAttempt = System.currentTimeMillis(); 
			
		try {
			Log.d(TAG, String.format("%d, %d", timeConnecting, timeout));
			/**
			 *  prevent reconnection -- additional measure,
			 *  taken because one of the functions in connect() clears the interrupt flag 
			 */
			while(!stopped) {
				ThreadUtil.throwIfInterruped();

				if (!connected) {
					connected = connect();
					
					if (connected) {
						Log.d(TAG, "connected successfully");
						timeConnecting = 0;
						
						onConnect();
					}
				}
				
				if (!connected) {
					Log.d(TAG, "connection failed");

					lastConnectionAttempt = System.currentTimeMillis();
					
					Log.v(TAG, "sleeping for " + sleepShortly);
					Thread.sleep(sleepShortly);
					
					timeConnecting += System.currentTimeMillis() - lastConnectionAttempt;
				} 
				
				if (timeConnecting > timeout) {
					Log.v(TAG, "sleeping for " + sleepLong);
					Thread.sleep(sleepLong);
					continue;
				}
				
				if (connected) {
					read();
				}
			}
		} catch (InterruptedException e) {
			Log.d(TAG, "exception", e);
		} catch (IOException e) {
			Log.d(TAG, "exception", e);
		} finally {
			
			if (connected) {
				// we set connected to false in order for client classes to observe isConnected status right 
				connected = false;
				onDisconnect();
			}
			
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

	private boolean connect() throws InterruptedException {
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
            
            return true;
		} catch(IOException ioe) {
			Log.d(TAG, "", ioe);
			
			
			return false;
		} 
	}
	
	protected abstract void onConnect() throws IOException, InterruptedException;
	
	protected abstract void read() throws IOException, InterruptedException;

	protected abstract void onDisconnect();
		
	public boolean isConnected() {
		return connected;
	}
	
	public void closeSocket() {
		if (socket != null) {
			try {
				stopped  = true;

				socket.close();				
			} catch (IOException e) {
				Log.v(TAG, "-", e);
			}
		}
	}
}
