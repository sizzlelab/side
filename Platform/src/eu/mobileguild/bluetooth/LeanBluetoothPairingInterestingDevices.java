/*******************************************************************************
o * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import eu.mobileguild.ui.ListItemOnClickListener;
import eu.mobileguild.utils.BluetoothUtil;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverStatusListener;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.SinkDriverConnection;

public class LeanBluetoothPairingInterestingDevices extends Activity implements 
	ListView.OnItemClickListener, 
	OnClickListener,
	DriverStatusListener
	{
	    
	private static final String NAME_OF_DEVICE_BEING_TESTED = "name of device being tested";

	private static final String TAG = LeanBluetoothPairingInterestingDevices.class.getSimpleName();
	
	public static final String INTERESTING_DEVICE_NAME_PREFIX = "deviceName.prefix";
	
	public static final String AVAILABLE_DEVICE_ADDRESS = "device.address";
	
	public static final String BORING_DEVICE_ADDRESS = "device.boring";
	
	public static final String DISCONNECT_WHEN_DONE = "disconnect";

	private static final int POSTPONE_TERMINATION_TIME = 10000;

	public static final String DRIVER_ACTION = "driver action";

	private static final String CONNECTION_TIMEOUT = "connection time to live";
	
	private SmartBluetoothDeviceAdapter listAdapter;

	private ListView listView;

	private String boringDeviceAddress;

	private BroadcastReceiver broadcastReceiver;

	private ProgressBar scanningProgress;
	
	private Button scanButton;

	private BluetoothConnectionTestTask testDeviceTask;

	private String deviceNamePrefix;

	private String driverAction;

	private SinkDriverConnection connection;
	
	private String clientId = LeanBluetoothPairingInterestingDevices.class.getName();

	private Handler handler;

	private String deviceToConnect;

	private ProgressDialog progressDialog;

	private Runnable terminator = new Terminator();

	private boolean disconnectWhenDone;
	
	
    @Override
	protected void onCreate(Bundle sis) {
    	Log.d(TAG, "onCreate");
        super.onCreate(sis);
        
        requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.smart_bluetooth_pairing_step2);

		setRequestedOrientation(getResources().getConfiguration().orientation);
		
		if (getIntent() != null) {
			final Bundle b = getIntent().getExtras();
			boringDeviceAddress = b.getString(BORING_DEVICE_ADDRESS);
			deviceNamePrefix = b.getString(INTERESTING_DEVICE_NAME_PREFIX);
			driverAction = b.getString(DRIVER_ACTION);
			disconnectWhenDone = b.containsKey(DISCONNECT_WHEN_DONE) ? b.getBoolean(DISCONNECT_WHEN_DONE): false;
					
		} else {
			boringDeviceAddress = sis.getString(BORING_DEVICE_ADDRESS);
			deviceNamePrefix = sis.getString(INTERESTING_DEVICE_NAME_PREFIX);
			driverAction = sis.getString(DRIVER_ACTION);
			deviceToConnect = sis.getString(NAME_OF_DEVICE_BEING_TESTED);
			disconnectWhenDone = sis.getBoolean(DISCONNECT_WHEN_DONE);
		}
    	            	
    	scanningProgress = (ProgressBar) findViewById(android.R.id.progress);
    	
    	scanButton = (Button) findViewById(R.id.scan);
    	scanButton.setOnClickListener(this);
    	    
		final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    	
		listAdapter = new SmartBluetoothDeviceAdapter(
				this, 
				R.layout.smart_bluetooth_device_item, 
				transformBluetoothDevice(btAdapter.getBondedDevices()));
	
		listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(listAdapter);
		
		handler = new Handler();
		
		bindToDriver(driverAction);
	}
     
    @Override
    public void onResume() {
    	Log.d(TAG, "onResume  " + deviceToConnect);
    	super.onResume();
    	
    	// Bluetooth is not enabled. Will launch system activity to enable it and come back
    	if (BluetoothUtil.enablingBluetooth(this)) {
    		return;
    	}

    	// if we are not connecting to anywhere, lets start looking for device candidates
    	if (!BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
    		startDiscovery();
    	}
    }


	private void bindToDriver(final String driverAction) {
		connection = new SinkDriverConnection(driverAction, clientId);
		
		connection.addDriverStatusListener(this);

		connection.bind(this);
	}
   
	@Override
    public void onSaveInstanceState(Bundle sis) {
    	
    	sis.putString(BORING_DEVICE_ADDRESS, boringDeviceAddress);
    	sis.putString(INTERESTING_DEVICE_NAME_PREFIX, deviceNamePrefix);
    	sis.putString(DRIVER_ACTION, driverAction);
    	sis.putString(NAME_OF_DEVICE_BEING_TESTED, deviceToConnect);	
    	sis.putBoolean(DISCONNECT_WHEN_DONE, disconnectWhenDone);
    }
	
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause");
    	super.onPause();
    	    	    	
    	handler.removeCallbacks(terminator);
    	
    	final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter.isDiscovering()) {
    		adapter.cancelDiscovery();
		}
    	
    	try { 
    		unregisterReceiver(broadcastReceiver);

    	} catch(Exception e) { }
    	
    	if (progressDialog != null && progressDialog.isShowing()) {
    		progressDialog.dismiss();
    	}    	
    }		

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.d(TAG, "onDestroy");
    	
    	if (connection == null) {
    		return;
    	}
    	
		if (disconnectWhenDone) {
			connection.sendDisconnectRequest();
		}
		
		connection.unbind(this);
    }

	@Override
	public void onClick(View v) {
		
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		if (v.getId() == R.id.scan && !adapter.isDiscovering()) {			
			startDiscovery();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "onItemClick");

		final BluetoothDevice device = listAdapter.getItem(position).device;
		
		connect(device);
	}


	private void connect(final BluetoothDevice device) {
		Log.d(TAG, "connect " + device.getName());
		
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		final boolean discovering = adapter.isDiscovering();
		
		final String name = device.getName() != null ? device.getName() : getString(R.string.unknown); 
		
		final String message = discovering 
				? getString(R.string.stopping_discovery) + " " + getString(R.string.checking_device, name) 
				: getString(R.string.checking_device, name);
				
		
		progressDialog = ProgressDialog.show(this, "", message, true);
		
		deviceToConnect = device.getAddress();
		
		if (discovering) {
			adapter.cancelDiscovery();
			// We will start connection in onDiscoveryFinished method
		} else {
			
			startOrBind(driverAction, deviceToConnect);
		}
	}


	private void startOrBind(String action, final String address) {
		
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		final Editor editor = prefs.edit();
		
		editor.putLong(CONNECTION_TIMEOUT, System.currentTimeMillis() + POSTPONE_TERMINATION_TIME);
		editor.commit();
		
		handler.postDelayed(terminator, POSTPONE_TERMINATION_TIME + /* just in case buffer */ 1000);
		
		if (connection.getDriverStatus() == DriverStatusListener.UNBOUND) {
			throw new RuntimeException("Shouldn't happen! : ");
		}
	
		Log.d(TAG, "startOrBind " + connection.getDriverStatus());
		connection.sendStartConnecting(address);
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");
		
		// we open bluetooth the fist thing as we get into this dialog
		if (requestCode == BluetoothUtil.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			Toast.makeText(this, R.string.rejected_bt_on, Toast.LENGTH_LONG).show();
			
			setResult(Activity.RESULT_CANCELED);
			finish();
			return;
		} else
		if (requestCode == BluetoothUtil.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
			final Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
			for (SmartBluetoothDevice device : transformBluetoothDevice(bondedDevices)) {
				listAdapter.add(device);
			}
		}
	}
	
	class SmartBluetoothDevice { 
		
		public SmartBluetoothDevice(BluetoothDevice device) {
			this.device = device;
		}
		
		public final static int UNCHECKED = 0;
		public final static int IRRESPONSIVE = 1;
		
		final BluetoothDevice device;
		
		int status;
	}
	
	class SmartBluetoothDeviceAdapter extends ArrayAdapter<SmartBluetoothDevice> {

		private LayoutInflater inflater;

		public SmartBluetoothDeviceAdapter(Context context, int textViewResourceId, List<SmartBluetoothDevice> devices) {
			super(context, textViewResourceId, devices);
			
			inflater = LayoutInflater.from(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.smart_bluetooth_device_item, parent, false);
			}
			
			final TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
			final TextView addressView = (TextView) convertView.findViewById(R.id.device_address);
			
			final Button connectButton = (Button) convertView.findViewById(R.id.connect);  
			connectButton.setOnClickListener(new ListItemOnClickListener(
					LeanBluetoothPairingInterestingDevices.this, 
					null,
					position,
					position));
			
			
			final SmartBluetoothDevice item = getItem(position);

			nameView.setText(item.device.getName());
			addressView.setText(item.device.getAddress());
			
			if (item.status == SmartBluetoothDevice.IRRESPONSIVE) {
				connectButton.setText(R.string.reconnect);
			}
			
			return convertView;
		}
	}
	
	private ArrayList<SmartBluetoothDevice> transformBluetoothDevice(Set<BluetoothDevice> devices) {
		
		final ArrayList<SmartBluetoothDevice> smartDevices = new ArrayList<SmartBluetoothDevice>();
		
		for (BluetoothDevice device : devices) {
			if (!isInteresting(device)) {
				continue;
			}
			smartDevices.add(new SmartBluetoothDevice(device));
		}
		
		return smartDevices;
	}

	private boolean isInteresting(BluetoothDevice device) {
		final String name = device.getName();
		
		if (device.getAddress().equals(boringDeviceAddress)) {
			return false;	
		}
		
		return  name != null && 
				name.length() >= 6 && 
				name.substring(0, 6).toLowerCase().equals(deviceNamePrefix);
	}
	
	private void startDiscovery() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (broadcastReceiver == null) {
			broadcastReceiver = new DisoveringDevices();
			
			final IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			filter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
			
			registerReceiver(broadcastReceiver, filter); 
		}
		
		BluetoothAdapter.getDefaultAdapter().startDiscovery();
		
		scanButton.setText(R.string.scanning);
		scanButton.setEnabled(false);
		scanningProgress.setVisibility(View.VISIBLE);
	}
	
	class DisoveringDevices extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
	    	Log.d(TAG, intent.getAction());
	    		    	
	        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
	        	onDeviceFound(context, intent);
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
	        	onDiscoveryFinished(context, intent);
	        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
	        	onBondChanged(context, intent);	        	
	        }
		}
	}

	public void onDeviceFound(Context context, Intent intent) {

    	final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		
		if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
			// device is already in the list;
			return;
		}
		
		if (!isInteresting(device)) {
			return;
		}
		
    	listAdapter.add(new SmartBluetoothDevice(device));
	}

	private void onBondChanged(Context context, Intent intent) {
		Log.d(TAG, "onBondChanged");
		
		final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

		int newState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
		
		
		if (newState == BluetoothDevice.BOND_BONDING && 
			testDeviceTask != null && 
			testDeviceTask.getDevice().getAddress().equals(device.getAddress())) {
			
			testDeviceTask.postpone(POSTPONE_TERMINATION_TIME);
		}
	}


	public void onDiscoveryFinished(Context context, Intent intent) {
		Log.d(TAG, "onDiscoveryFinished");
		
		scanButton.setEnabled(true);
		scanButton.setText(R.string.scan);
		scanningProgress.setVisibility(View.GONE);
				
		if (deviceToConnect == null) {
			return;
		}
		
		startOrBind(driverAction, deviceToConnect);
	}

	@Override
	public void onDriverStatusChanged(DriverConnection connection, int newStatus) {
		Log.d(TAG, String.format("onDriverStatusChanged (%s) = %d", connection.getDriverAction(), newStatus));
		
    	final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    	
    	final long timeout = prefs.getLong(CONNECTION_TIMEOUT, 0);
		
    	// user comes back from another application, while pairing has been happening at the background
    	if (newStatus == DriverStatusListener.CONNECTING && deviceToConnect != null && timeout > System.currentTimeMillis()) {
    		final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceToConnect);
    		
    		final String name = device.getName() != null ? device.getName() : getString(R.string.unknown);
    		progressDialog = ProgressDialog.show(this, "", getString(R.string.checking_device, name));
    		
    		// TODO: refactor to use Timer instead of Handler
    		handler.postAtTime(terminator, timeout);
    		
    		return;
    	}		
		
		
		if (newStatus != DriverStatusListener.CONNECTED) {
			return;
		}
		
		Toast.makeText(
				this, 
				R.string.paring_sucessful, 
				Toast.LENGTH_LONG).show();
		
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		final Intent result = new Intent();
		
		if (deviceToConnect == null) {
			deviceToConnect = ((SinkDriverConnection) connection).getDeviceAddress();
		}
		result.putExtra(AVAILABLE_DEVICE_ADDRESS, deviceToConnect);
		
		setResult(Activity.RESULT_OK, result);
		finish();
	}
	
	class Terminator implements Runnable {
		
		@Override
		public void run() {
			Log.d(TAG, "Terminator:run()");
			progressDialog.dismiss();

			connection.sendDisconnectRequest();
			
			updateList();

			deviceToConnect = null;
		}

		private void updateList() {
			for (int i = 0; i<listAdapter.getCount(); i++) {
				
				final SmartBluetoothDevice smartDevice = listAdapter.getItem(i);
				
				if (!smartDevice.device.getAddress().equals(deviceToConnect)) {
					continue;
				}
				
				smartDevice.status = SmartBluetoothDevice.IRRESPONSIVE;
				listView.invalidateViews();
				return;
			}
		}
	};
}
