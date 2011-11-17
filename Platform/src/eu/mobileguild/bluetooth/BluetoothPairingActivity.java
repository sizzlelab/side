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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

		private ProgressDialog progressDialog;

		private boolean backButtonPressed = false;
		
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
	    	
	    	if (!backButtonPressed) {
	    		return;
	    	}
	    	
	    	stopThreads();
	    	
	    }

		private void stopThreads() {
			
			synchronized(threads) {
				Log.d(TAG, "stopThreads" + threads.size());
				for(ConnectThread t: threads) {
					
					if (!t.isAlive()) {
						continue;
					}
					
		    		try {
		    			Log.d(TAG, "Close socket");
						t.closeSocket();
		    			Log.d(TAG, "Closed socket");

					} catch (IOException e) {
						Log.d(TAG, "-", e);
					}
		    		
		    		
		    		t.interrupt();
		    	}
				
				threads.clear();
			}
		}
		
	    @Override
	    public void onBackPressed() {
	    	super.onBackPressed();
	    	backButtonPressed  = true;
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
					
					Toast.makeText(BluetoothPairingActivity.this, R.string.pairing_problem, Toast.LENGTH_LONG).show();
					
					progressDialog.cancel();
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
					Toast.makeText(context, R.string.pairing_problem, Toast.LENGTH_LONG).show();
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

		private ArrayList<String> usedAdresses = new ArrayList<String>();

		private ArrayList<ConnectThread> threads = new ArrayList<ConnectThread>();
		
		protected boolean isInterestingDevice(String name) {
			return name.length() > 3;
		}
			
		protected void connectToTheDevice(String deviceAddress, UUID foundUUID) {
			
	        if (Collections.binarySearch(usedAdresses , deviceAddress) >= 0) {
	        	return;
	        }
	        usedAdresses.add(deviceAddress);
	        
			Log.d(TAG, "connectToDevice " + deviceAddress);
	        ConnectThread connectThread = new ConnectThread(deviceAddress, foundUUID);       
	        connectThread.start();		
	        
	        synchronized(threads) {
	        	threads.add(connectThread);
	        }
		}


		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			final String name = (String) ((TextView)view.findViewById(R.id.device_name)).getText();
			
	        if (!isInterestingDevice(name)) {
	        	Toast.makeText(BluetoothPairingActivity.this, R.string.not_interesting_device, Toast.LENGTH_LONG).show();
	        	return; 
	        }

	        stopThreads();
	        usedAdresses.clear();

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
			
			stopThreads();
			finish();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}

		class ConnectThread extends Thread {

			private String address;
			private UUID uuid;
			private BluetoothSocket socket;

			public ConnectThread(String address, UUID foundUUID) {
				this.address = address;
				this.uuid = foundUUID;
			}
			
			public void closeSocket() throws IOException {
				
				if (socket == null) {
					return;
				}
				socket.close();
			}

			@Override
			public void run() {
				Log.d("ConnectRunnable", "ConnectRunnable::run ");

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
					    
		        } catch (Exception e) {
		        	BluetoothPairingActivity.this.runOnUiThread(new Runnable() {
		        		@Override
		        		public void run() {
				        	Toast.makeText(
									BluetoothPairingActivity.this, 
									R.string.pairing_problem, 
									Toast.LENGTH_LONG).show();	
		        		}
		        	});
		        	
					Log.d(TAG, "exception", e);
				} finally {
					Log.d(TAG, "finally");
					
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							Log.d(TAG, "", e);
						}					
					}
					Log.d(TAG, "foobar");
					socket = null;
				}
			}
			
			private void printMessage(byte[] timeInformation) {
				StringBuilder builder = new StringBuilder();
				
				for(int i=0;i<timeInformation.length; i++) {
					builder.append(String.format("%x:", timeInformation[i]));
				}
				
				Log.d("ConnectRunnable", "message: " + builder.toString());
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
