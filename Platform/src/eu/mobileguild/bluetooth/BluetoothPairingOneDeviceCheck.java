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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import eu.mobileguild.utils.BluetoothUtil;
import fi.hut.soberit.sensors.R;

public class BluetoothPairingOneDeviceCheck extends Activity implements 
	OnClickListener,
	BluetoothConnectionTestTask.ResultListener
	{
	    
	private static final String TAG = BluetoothPairingOneDeviceCheck.class.getSimpleName();

	public static final String DEVICE_ADDRESS = "previous.address";
	public static final String PROCEED_TO_SECOND_STEP = "proceed.second_step";

	private static final int REQUEST_PAIRING_STEP_TWO = 11;
	
	public static final String INTERESTING_DEVICE_NAME_PREFIX = BluetoothPairingInterestingDevices.INTERESTING_DEVICE_NAME_PREFIX;
	

	private TextView instructionsView;

	private TextView deviceNameView;

	private TextView deviceAddressView;

	private ViewGroup deviceControls;

	private Button nextAction;

	private boolean secondStep;

	private Button skipDevice;

	private BluetoothConnectionTestTask testDeviceTask;

	private String deviceNamePrefix;

	private ViewGroup nextActionControls;
	
	
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

        setContentView(R.layout.smart_bluetooth_pairing_step1);

		setRequestedOrientation(getResources().getConfiguration().orientation);
		
		String deviceAddress = null;
		
		if (getIntent() != null) {
			final Bundle b = getIntent().getExtras();
			deviceAddress = b.getString(DEVICE_ADDRESS);
			secondStep = b.getBoolean(PROCEED_TO_SECOND_STEP);
			deviceNamePrefix = b.getString(INTERESTING_DEVICE_NAME_PREFIX);
					
		} else {
			deviceAddress = sis.getString(DEVICE_ADDRESS);
			secondStep = sis.getBoolean(PROCEED_TO_SECOND_STEP);
			deviceNamePrefix = sis.getString(INTERESTING_DEVICE_NAME_PREFIX);
		}
		
		if (deviceAddress == null && !secondStep) {
			throw new RuntimeException("Shouldn't happen!");
		}
		
		deviceControls = (ViewGroup) findViewById(R.id.device_controls);
		nextActionControls = (ViewGroup) findViewById(R.id.next_action_controls);
		
		
    	instructionsView = (TextView) findViewById(R.id.instructions);
    	
    	deviceNameView = (TextView) findViewById(R.id.old_device_name);
    	deviceAddressView = (TextView) findViewById(R.id.old_device_address);
    	
    	skipDevice = (Button) findViewById(R.id.skip);
    	skipDevice.setVisibility(secondStep ? View.VISIBLE : View.GONE);
    	skipDevice.setOnClickListener(this);
    	
    	
    	nextAction = (Button) findViewById(R.id.next_action);
    	nextAction.setText(secondStep ? R.string.skip_forward_button : R.string.close);
    	nextAction.setOnClickListener(this);
    	
    	findViewById(R.id.retry).setOnClickListener(this);
    	
    	
    	findViewById(R.id.connect).setOnClickListener(this);
    		        
		final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    			
		final BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
		
		deviceNameView.setText(device.getName());
		deviceAddressView.setText(device.getAddress());
		
	}
     
    @Override
    public void onResume() {
    	Log.d(TAG, "onResume  " + deviceNameView.getText());
    	super.onResume();

    	
    	// If Bluetooth is not enabled, launch system activity to enable it and come back
    	if (BluetoothUtil.enablingBluetooth(this)) {
    		deviceControls.setVisibility(View.GONE);
    		return;
    	}

		if (deviceAddressView.getText() == null) {
			deviceControls.setVisibility(View.GONE);
			nextAction.setVisibility(View.VISIBLE);
			
			return;
		}
    }
   
	@Override
    public void onSaveInstanceState(Bundle sis) {
    	
    	sis.putString(DEVICE_ADDRESS, deviceAddressView.getText().toString());
    	sis.putBoolean(PROCEED_TO_SECOND_STEP, secondStep);
    	sis.putString(INTERESTING_DEVICE_NAME_PREFIX, deviceNamePrefix);
    }
	
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause");
    	super.onPause();
    	
    	if (testDeviceTask != null) {
    		testDeviceTask.cancel(true);
    	}
    }		


	@Override
	public void onClick(View v) {
		
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		final String deviceAddress = deviceAddressView.getText().toString();
		if (v.getId() == R.id.connect) {
			
			testDeviceTask = new BluetoothConnectionTestTask(this, adapter.getRemoteDevice(deviceAddress));
			testDeviceTask.setListener(this);
			testDeviceTask.execute();
		} else 
		if ((v.getId() == R.id.skip || v.getId() == R.id.next_action) && secondStep) {
			
			final Intent intent = new Intent(this, BluetoothPairingInterestingDevices.class);

			intent.putExtra(BluetoothPairingInterestingDevices.DEVICE_ADDRESS, deviceAddress);
			intent.putExtra(BluetoothPairingInterestingDevices.INTERESTING_DEVICE_NAME_PREFIX, deviceNamePrefix);
			startActivityForResult(intent, REQUEST_PAIRING_STEP_TWO);

		} else
		if (v.getId() == R.id.next_action && !secondStep) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		} else 
		if (v.getId() == R.id.retry) { 
			testDeviceTask = new BluetoothConnectionTestTask(this, adapter.getRemoteDevice(deviceAddress));
			testDeviceTask.setListener(this);
			testDeviceTask.execute();
		}
	}

	@Override
	public void onConnectionTestFinish(boolean wasConnected, BluetoothDevice device) {
		
		testDeviceTask = null;
		
		if (wasConnected) {
			Toast.makeText(
				BluetoothPairingOneDeviceCheck.this, 
				R.string.paring_sucessful, 
				Toast.LENGTH_LONG).show();
	
			final Intent result = new Intent();
			result.putExtra(DEVICE_ADDRESS, device.getAddress());
			
			BluetoothPairingOneDeviceCheck.this.setResult(Activity.RESULT_OK, result);
			finish();
		} else {
			deviceControls.setVisibility(View.GONE);
			nextActionControls.setVisibility(View.VISIBLE);
			
			final String message = secondStep 
					? getString(R.string.old_device_unresponsive) + getString(R.string.try_scanning_devices)
					: getString(R.string.old_device_unresponsive);
			
			instructionsView.setText(message);
			nextAction.setVisibility(View.VISIBLE);
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
			deviceControls.setVisibility(View.VISIBLE);
		} else 
		if (requestCode == REQUEST_PAIRING_STEP_TWO) {
			
			setResult(resultCode, data);
			finish();
		}
	}


}
