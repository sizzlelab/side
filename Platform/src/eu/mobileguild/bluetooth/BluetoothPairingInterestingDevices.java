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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
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
import fi.hut.soberit.sensors.R;

public class BluetoothPairingInterestingDevices extends Activity implements 
	ListView.OnItemClickListener, 
	OnClickListener,
	BluetoothConnectionTestTask.ResultListener
	{
	    
	private static final String TAG = BluetoothPairingInterestingDevices.class.getSimpleName();
	
	public static final String INTERESTING_DEVICE_NAME_PREFIX = "deviceName.prefix";
	
	public static final String DEVICE_ADDRESS = "device.address";

	private static final int POSTPONE_TERMINATION_TIME = 10000;
	
	private SmartBluetoothDeviceAdapter listAdapter;

	private ListView listView;

	private String boringDeviceAddress;

	private BroadcastReceiver broadcastReceiver;

	private ProgressBar scanningProgress;
	
	private Button scanButton;

	private BluetoothConnectionTestTask testDeviceTask;

	private String deviceNamePrefix;
	
	@Override
	protected void onRestoreInstanceState(Bundle sis) {
		super.onRestoreInstanceState(sis);
		Log.d(TAG, "onRestoreInstanceState ");
	}
		
				
    @Override
	protected void onCreate(Bundle sis) {
    	Log.d(TAG, "onCreate");
        super.onCreate(sis);
        
        requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.smart_bluetooth_pairing_step2);

		setRequestedOrientation(getResources().getConfiguration().orientation);
		
		if (getIntent() != null) {
			final Bundle b = getIntent().getExtras();
			boringDeviceAddress = b.getString(DEVICE_ADDRESS);
			deviceNamePrefix = b.getString(INTERESTING_DEVICE_NAME_PREFIX);
					
		} else {
			boringDeviceAddress = sis.getString(DEVICE_ADDRESS);
			deviceNamePrefix = sis.getString(INTERESTING_DEVICE_NAME_PREFIX);
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
	}
     
    @Override
    public void onResume() {
    	Log.d(TAG, "onResume  " + boringDeviceAddress);
    	super.onResume();
    	
    	// Bluetooth is not enabled. Will launch system activity to enable it and come back
    	if (BluetoothUtil.enablingBluetooth(this)) {
    		return;
    	}
    	
    	if (!BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
    		startDiscovery();
    	}
    }
   
	@Override
    public void onSaveInstanceState(Bundle sis) {
    	
    	sis.putString(DEVICE_ADDRESS, boringDeviceAddress);
    	sis.putString(INTERESTING_DEVICE_NAME_PREFIX, deviceNamePrefix);
    }
	
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause");
    	super.onPause();
    	    	    	
    	final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter.isDiscovering()) {
    		adapter.cancelDiscovery();
		}
    	
    	try { 
    		unregisterReceiver(broadcastReceiver);
    	} catch(Exception e) { }
    	
    	if (testDeviceTask != null) {
    		testDeviceTask.cancel(true);
    	}
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

		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		final boolean discovering = adapter.isDiscovering();
		
		final BluetoothDevice device = listAdapter.getItem(position).device;
		
		final String name = device.getName() != null ? device.getName() : "unknown"; 
		
		final String message = discovering 
				? getString(R.string.stopping_discovery) + " " + getString(R.string.checking_device, name) 
				: getString(R.string.checking_device, name);
				
		
		testDeviceTask = new BluetoothConnectionTestTask(
				this,
				device,
				message);
		testDeviceTask.setListener(this);
		
		if (discovering) {
			adapter.cancelDiscovery();
			// Thread will be started in onDiscoveryFinished method
		} else {
			testDeviceTask.execute();
		}
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");
		
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
					BluetoothPairingInterestingDevices.this, 
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
		final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

		int newState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
		
		
		if (newState == BluetoothDevice.BOND_BONDING && 
			testDeviceTask != null && 
			testDeviceTask.getDevice().getAddress().equals(device.getAddress())) {
			
			testDeviceTask.postpone(POSTPONE_TERMINATION_TIME);
		}
	}


	public void onDiscoveryFinished(Context context, Intent intent) {
		scanButton.setEnabled(true);
		scanButton.setText(R.string.scan);
		scanningProgress.setVisibility(View.GONE);
		
		if (testDeviceTask != null) {
			testDeviceTask.execute();
		}
	}


	@Override
	public void onConnectionTestFinish(boolean wasConnected, BluetoothDevice device) {
		// important! this is how we identify whenever this thread is running
		testDeviceTask = null;
		
		if (wasConnected) {
		
			Toast.makeText(
				this, 
				R.string.paring_sucessful, 
				Toast.LENGTH_LONG).show();
	
			final Intent result = new Intent();
			result.putExtra(DEVICE_ADDRESS, device.getAddress());
			
			setResult(Activity.RESULT_OK, result);
			finish();
		} else {
			for (int i = 0; i<listAdapter.getCount(); i++) {
				
				final SmartBluetoothDevice smartDevice = listAdapter.getItem(i);
				
				if (!smartDevice.device.getAddress().equals(device.getAddress())) {
					continue;
				}
				
				smartDevice.status = SmartBluetoothDevice.IRRESPONSIVE;
				listView.invalidateViews();
				return;
			}

		}

	}
}
