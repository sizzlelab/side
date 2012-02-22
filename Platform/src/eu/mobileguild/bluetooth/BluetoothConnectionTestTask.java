package eu.mobileguild.bluetooth;

import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import fi.hut.soberit.sensors.R;

class BluetoothConnectionTestTask extends AsyncTask<Void, Integer, Void> {
	
	public static final String TAG = BluetoothConnectionTestTask.class.getSimpleName();

	private static final int LONG_TIMEOUT_TO_TEST_DEVICE = 5000;
	
	private static final UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

	
	private ProgressDialog progressDialog;
	private Activity activity;
	private ResultListener listener;
	protected OpenConnectionThread openConnectionThread;
	
	private Terminator terminator;

	private String message;
	
	public BluetoothConnectionTestTask(Activity activity, BluetoothDevice device) {
		this(activity, device, activity.getString(R.string.checking_device, device.getName()));
	}
	
	
	public BluetoothConnectionTestTask(Activity activity, BluetoothDevice device, String message) {
		this(activity, device, DEFAULT_UUID, true, LONG_TIMEOUT_TO_TEST_DEVICE);
		
		this.message = message;
	}
	
	
	public BluetoothConnectionTestTask(Activity activity, BluetoothDevice device, UUID uuid, boolean tryInsecure, int timeout) {
		this.activity = activity;
				
		openConnectionThread = new OpenConnectionThread(device, uuid, tryInsecure);
		
		terminator = new Terminator(openConnectionThread, timeout);
	}
	
	
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(activity, "", message, true, true);
	}
	
	
	@Override
	protected Void doInBackground(Void... params) {
		terminator.start();
		openConnectionThread.run();
		
		return null;
	}
	
	protected void onPostExecute(Void foobar) {
		progressDialog.dismiss();

		if (listener != null) {
			listener.onConnectionTestFinish(openConnectionThread.wasConnected(), openConnectionThread.getDevice());
		}
	}
	
	public void setListener(ResultListener listener) {
		this.listener = listener;
	}
	
	
	public static interface ResultListener {
		public void onConnectionTestFinish(boolean wasConnected, BluetoothDevice device);
	}
	
	public void postpone(int time) {
		terminator.postpone(time);
	}
	
	public BluetoothDevice getDevice() {
		return openConnectionThread.getDevice();
	}
	
}

class Terminator implements Runnable {
	
	public static String TAG = Terminator.class.getSimpleName();
	
	private OpenConnectionThread runnable;

	int nextTimeout = 0;

	private Handler handler;
	
	public Terminator(OpenConnectionThread runnable, int timeout) {
		this.runnable = runnable;
		this.nextTimeout = timeout; 
		
		handler = new Handler();
	}
	
	public synchronized void start() {
		handler.postDelayed(this, nextTimeout);
		nextTimeout = 0;
	}
	
	
	@Override
	public synchronized void run() {
		synchronized(runnable.getMonitor()) {
			if (nextTimeout == 0 && !runnable.wasConnected()) {
				Log.d(TAG, "closing socket for " + runnable.getDevice().getAddress());
				runnable.closeSocket();
			}
			
			if (nextTimeout != 0) {
				handler.postDelayed(this, nextTimeout);
				nextTimeout = 0;
			}
		}
	}
	
	public synchronized void postpone(int time) {
		Log.d(TAG, "postpone " + time);

		nextTimeout = time;
	}

}