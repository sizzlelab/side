package eu.mobileguild.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import eu.mobileguild.ui.MultidimensionalArrayAdapter;
import fi.hut.soberit.sensors.R;

public class BluetoothPairingActivity extends Activity implements ListView.OnItemClickListener, OnClickListener {
	    
		private static final String TAG = BluetoothPairingActivity.class.getSimpleName();

		private static final String ACTION_FIELD_UUID = "android.bluetooth.device.extra.UUID";

		private static final String ACTION_BLUETOOTH_UUID = "android.bleutooth.device.action.UUID";

		public static  UUID targetUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
		
		public static final String INTENT_DEVICE_ADDRESS = "device_address";

		private MultidimensionalArrayAdapter listAdapter;

		private boolean scanningInProcess = false;

		protected int counter = 1;
		
		protected String deviceAddress;

		private Thread connectThread;

		private ProgressDialog progressDialog;
		
	    @Override
		protected void onCreate(Bundle savedInstanceState) {
	        // Be sure to call the super class.
	        super.onCreate(savedInstanceState);
	                
	        requestWindowFeature(Window.FEATURE_PROGRESS);

	        setContentView(R.layout.bluetooth_devices_dialog);

	    	BluetoothPairingActivity.this.getWindow().setFeatureInt(
	    			Window.FEATURE_PROGRESS, 
	    			(counter -1) * 10000 /counter);
	        
	        scanButton = (Button) findViewById(R.id.scan_button);
	        scanButton.setOnClickListener(this);
	        
			setRequestedOrientation(getResources().getConfiguration().orientation);
	    }
	     
	    @Override
	    protected void onPause() {
	    	super.onPause();
	    	Log.d(TAG, "onPause");
	    	
	    	
	    	if (progressDialog != null) {
	    		progressDialog.cancel();
	    	}
	    }
		
	    @Override
		protected void onDestroy() {
	    	super.onDestroy();
	    	Log.d(TAG, "onDestroy");
	    	
	    	try {
	    		unregisterReceiver(broadcastReceiver);
	    	} catch(IllegalArgumentException e) {
	    		Log.d(TAG, "receiver wasn't registered", e);
	    	}
	    }
		
		
		private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		    	Log.d(TAG, intent.getAction());
		    		    	
		    	if (intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)) {
		    		BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		    		BluetoothClass bt = device.getBluetoothClass();
					Log.d(TAG, "class" + bt.toString());
		    	}
		    	
		        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
		        	onDeviceFound(context, intent);
		        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
		        	onDiscoveryFinished(context, intent);
		        } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
		        	onBondStateChanged(context, intent);
		        } else if (ACTION_BLUETOOTH_UUID.equals(intent.getAction())) {
		            onUuidReceived(context, intent);
		        }
		    }

			private void onUuidReceived(Context context, Intent intent) {
				Parcelable[] uuidExtra = intent.getParcelableArrayExtra(ACTION_FIELD_UUID);

				for(String key: intent.getExtras().keySet()) {
					Log.d(TAG, "Key:" + key);
				}
				
				if (uuidExtra == null) {
					Log.d(TAG, "uuidExtra == null");
					return;
				}
				
				final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);				
				Log.d(TAG, "testing " + device.getAddress());
				if (deviceAddress == null || !deviceAddress.equals(device.getAddress())) {
					return;
				}
				
				boolean found = false;
				UUID foundUUID = null;
				for(Parcelable puuid: uuidExtra) {
					Log.d(TAG, puuid.toString());
					
					if (puuid.toString().equals(targetUUID.toString())) {
						found = true;
						foundUUID = ((ParcelUuid)puuid).getUuid();
						break;
					}
				}

				
				if (!found) {
					Toast.makeText(
							BluetoothPairingActivity.this, 
							R.string.no_appropriate_service, 
							Toast.LENGTH_LONG).show();
					progressDialog.cancel();
					return;
				}

				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					Toast.makeText(context, R.string.paring_sucessful, Toast.LENGTH_LONG).show();
					progressDialog.cancel();
					finishPairing();
					return;
				} 

				Log.d(TAG, "connectToDevice");
				connectToTheDevice(deviceAddress, foundUUID);

			}

			private void onBondStateChanged(Context context, Intent intent) {
				final int bond = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);

				final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);				
				if (deviceAddress == null || 
					!deviceAddress.equals(device.getAddress()) ||
					bond == BluetoothDevice.BOND_BONDING) {
					return;
				}
				
				progressDialog.cancel();
				
				if (bond == BluetoothDevice.BOND_BONDED) {
					Toast.makeText(context, R.string.paring_sucessful, Toast.LENGTH_LONG).show();
					finishPairing();
				} else {
					Toast.makeText(context, R.string.pairing_problems, Toast.LENGTH_LONG).show();
				}
				
				deviceAddress = null;
			}

			private void onDeviceFound(Context context, Intent intent) {
				BluetoothPairingActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, counter);
		    	counter += 2;
		    	
		    	BluetoothPairingActivity.this.getWindow().setFeatureInt(
		    			Window.FEATURE_PROGRESS, 
		    			(counter - 1) * 10000 /counter);

	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            
	            for(String [] dev: deviceRecords) {
	            	if (dev[1].equals(device.getAddress())) {
	            		return;
	            	}
	            }
	            
	            listAdapter.addItem(new String[] {
	            		device.getName(),
	            		device.getAddress()
	            });			
			}
			
			private void onDiscoveryFinished(Context context, Intent intent) {
			    final int MAX_PROGRESS_VALUE = 10000;
				BluetoothPairingActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, MAX_PROGRESS_VALUE);
				((TextView)BluetoothPairingActivity.this.findViewById(R.id.scan_notice)).setText(R.string.finished_scanning);
				
	    		scanningInProcess = false;
			}
		};

		private Button scanButton;

		private ArrayList<String[]> deviceRecords;
		
		protected boolean isInterestingDevice(String name) {
			return name.length() > 3;
		}
			
		protected void connectToTheDevice(String connectToTheDevice2, UUID foundUUID) {
			
	        if (connectThread != null && connectThread.isAlive()) {
	        	return;        	
	        }
	        
	        connectThread = new Thread(new ConnectRunnable(deviceAddress, foundUUID));       
	        connectThread.start();			
		}


		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			final String name = (String) ((TextView)view.findViewById(R.id.device_name)).getText();
			
	        if (!isInterestingDevice(name)) {
	        	Toast.makeText(BluetoothPairingActivity.this, R.string.not_interesting_device, Toast.LENGTH_LONG).show();
	        	return; 
	        }

	        deviceAddress = (String) ((TextView)view.findViewById(R.id.device_address)).getText();
	        
	        final BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
			
			if (scanningInProcess) {
				defaultAdapter.cancelDiscovery();
				
				scanningInProcess = false;
			}
			
			final BluetoothDevice device = defaultAdapter.getRemoteDevice(deviceAddress);
			
			
	        progressDialog = ProgressDialog.show(BluetoothPairingActivity.this, "", getString(R.string.starting_pairing));
	        progressDialog.show();

			try {
				Class cl = Class.forName("android.bluetooth.BluetoothDevice");
	    		Class[] par = {};
	    		Method method = cl.getMethod("fetchUuidsWithSdp", par);
	    		Object[] args = {};
	    		method.invoke(device, args);
	    		
			} catch (Exception e) {
				Log.d(TAG, "", e);
			} 
		}
		
		/**
		 * Have selected a device and it is paired. Quit.
		 */	
		private void finishPairing() {
			final Intent returnIntent = new Intent();
			returnIntent.putExtra(INTENT_DEVICE_ADDRESS, deviceAddress);
			setResult(RESULT_OK, returnIntent);
			finish();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}

		class ConnectRunnable implements Runnable {

			private String address;
			private UUID uuid;

			public ConnectRunnable(String address, UUID foundUUID) {
				this.address = address;
				this.uuid = foundUUID;
			}
			
			@Override
			public void run() {
				Log.d("ConnectRunnable", "ConnectRunnable::run ");

				BluetoothSocket socket = null;
		        try {
					final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
					final BluetoothDevice device = adapter.getRemoteDevice(address);

					String methodName = null;
					if (Build.VERSION.SDK_INT < 10) {
						methodName = "createRfcommSocketToServiceRecord";
					} else {
						methodName = "createInsecureRfcommSocketToServiceRecord";
					} 

					Method m = device.getClass().getMethod(methodName, new Class[] { UUID.class});
					socket = (BluetoothSocket) m.invoke(device, uuid);

					socket.connect();
					
					byte [] buf = new byte[8];
					
					InputStream stream = socket.getInputStream();
					
					Log.d("ConnectRunnable", "stream::read "+ stream.read(buf));
					printMessage(buf);
					    
					if (socket != null) {
						socket.close();					
					}
		        } catch (Exception e) {
		        	BluetoothPairingActivity.this.runOnUiThread(new Runnable() {
		        		@Override
		        		public void run() {
				        	Toast.makeText(
									BluetoothPairingActivity.this, 
									R.string.pairing_problems, 
									Toast.LENGTH_LONG).show();	
		        		}
		        	});
		        	
					Log.d(TAG, "exception", e);
				} finally {
					Log.d(TAG, "finally");
				}
			}
			
			private void printMessage(byte[] timeInformation) {
				StringBuilder builder = new StringBuilder();
				
				for(int i=0;i<timeInformation.length; i++) {
					builder.append(String.format("%x:", timeInformation[i]));
				}
				
				Log.d("ConnectRunnable", "message: " + builder.toString());
			}


			private BluetoothSocket connect(int port) throws NoSuchMethodException,
					IllegalAccessException, InvocationTargetException,
					IOException {
				BluetoothSocket socket;
				final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				final BluetoothDevice device = adapter.getRemoteDevice(address);
				
				socket = device.createRfcommSocketToServiceRecord(uuid);

				
//				Method m = device.getClass().getMethod("createRfcommSocket",
//				        new Class[] { int.class });
//				socket = (BluetoothSocket)m.invoke(device, Integer.valueOf(port));
				    
		        try {
		            socket = connect(1);
					if (socket != null) {
						socket.close();					
					}
		        } catch (Exception e) {
		        	
		        	
					Log.d(TAG, "exception", e);
				} finally {
					Log.d(TAG, "finally");
				}
				    
				socket.connect();
				return socket;
			}
		
	}

	@Override
	public void onClick(View v) {

		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
						
		if (adapter == null) {
			return;
		}
		
		scanningInProcess = true;
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
		filter.addAction(ACTION_BLUETOOTH_UUID);
		
		registerReceiver(broadcastReceiver, filter); 
		
		if (!adapter.startDiscovery()) {
			return;
		}
		
		final ListView list = (ListView)findViewById(R.id.list);
		deviceRecords = new ArrayList<String[]>();
		listAdapter = new MultidimensionalArrayAdapter(
				BluetoothPairingActivity.this, 
        		R.layout.bluetooth_device_item, 
        		new int [] {R.id.device_name, R.id.device_address},
        		deviceRecords);
		list.setAdapter(listAdapter);
		
		scanButton.setVisibility(View.GONE);
		
		list.setVisibility(View.VISIBLE);
		findViewById(R.id.devices_separator).setVisibility(View.VISIBLE);
		((TextView)findViewById(R.id.scan_notice)).setText(R.string.scanning);
		
		list.setOnItemClickListener(BluetoothPairingActivity.this);
						
		BluetoothPairingActivity.this.findViewById(R.id.device_dialog_root).invalidate();
	}
}