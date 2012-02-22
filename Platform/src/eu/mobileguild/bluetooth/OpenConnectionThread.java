package eu.mobileguild.bluetooth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

public class OpenConnectionThread extends Thread {

	public static final String TAG = OpenConnectionThread.class.getSimpleName();
	
	private UUID uuid;

	private BluetoothSocket socket;
	
	private boolean tryInsecure;

	private BluetoothDevice device;

	private Object monitor = new Object();

	private boolean connected = false;
	
	public OpenConnectionThread(BluetoothDevice device, UUID targetUUID, boolean tryInsecure) {
		this.device = device;
		this.uuid = targetUUID;
		
		this.tryInsecure = tryInsecure;
	}
	
	@Override
	public void run() {
		Log.d(TAG, "ConnectRunnable::run ");

        try {			
			String methodName = null;
			if (Build.VERSION.SDK_INT >= 10 && tryInsecure) {
				methodName = "createInsecureRfcommSocketToServiceRecord";
			} else {
				methodName = "createRfcommSocketToServiceRecord";
			} 

			Method m = device.getClass().getMethod(methodName, new Class[] { UUID.class});
			socket = (BluetoothSocket) m.invoke(device, uuid);

			socket.connect();			    
			
			connected = true;
			
        } catch (Exception e) {
        			        	
        	onException();
        	
			Log.d(TAG, "exception", e);
		} finally {
			Log.d(TAG, "finally");
			
			synchronized(monitor) {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						Log.d(TAG, "", e);
					}
					socket = null;
				}
			}
		}        
	}

	protected void onException() {

	}
	
	
	public void closeSocket() {
		
		synchronized(monitor) {
			if (socket == null) {
				return;
			}

			
			try {
				socket.close();
				socket = null;
			} catch(IOException e) {
				return;
			}
		}
	}
	
	public Object getMonitor() {
		return monitor  ;
	}



	
	public BluetoothDevice getDevice() {
		return device;
	}

	public Boolean wasConnected() {
		return connected;
	}
}