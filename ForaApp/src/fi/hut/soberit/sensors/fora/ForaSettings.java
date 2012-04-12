/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * Maksim Golivkin <maksim@golivkin.eu>
 ******************************************************************************/
package fi.hut.soberit.sensors.fora;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import eu.mobileguild.bluetooth.BluetoothPairingActivity;
import eu.mobileguild.bluetooth.LeanBluetoothPairingInterestingDevices;
import fi.hut.soberit.fora.D40CachedSink;
import fi.hut.soberit.fora.D40Sink;
import fi.hut.soberit.fora.IR21Sink;

public class ForaSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	public static final String APP_PREFERENCES_FILE = "preferences";

	private static final String TAG = ForaSettings.class.getSimpleName();
		
	public static final String BROADCAST_FREQUENCY = "broadcast_frequency";

	public static final String D40_BLUETOOTH_NAME = "d40.bluetooth_name";
	public static final String D40_BLUETOOTH_ADDRESS = "d40.device_address";

	public static final String IR21_BLUETOOTH_NAME = "ir21.bluetooth_name";
	public static final String IR21_BLUETOOTH_ADDRESS = "ir21.device_address";

	
	public static final String TIMEOUT = "timeout";

	private static final int REQUEST_D40_ENABLE_BT = 13;
	private static final int REQUEST_IR21_ENABLE_BT = 15;
	private static final int REQUEST_FIND_D40 = 14;
	private static final int REQUEST_FIND_IR21 = 16;
		
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final PreferenceManager preferenceManager = getPreferenceManager(); 
		preferenceManager.setSharedPreferencesName(APP_PREFERENCES_FILE);
		addPreferencesFromResource(R.xml.preferences);
		
  		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
  		final SharedPreferences preferences = preferenceManager.getSharedPreferences();

  		final Preference broadcastingFrequencyPreference = findPreference(BROADCAST_FREQUENCY);
		final Preference d40BluetoothPreference = findPreference(D40_BLUETOOTH_NAME);
		final Preference ir21BluetoothPreference = findPreference(IR21_BLUETOOTH_NAME);
		final Preference timeoutPreference = findPreference(TIMEOUT);

		final String broadcastFrequencyString = 
			preferences.getString(BROADCAST_FREQUENCY, "") 
				+ " " 
				+ getString(R.string.milliseconds_short);
		broadcastingFrequencyPreference.setSummary(broadcastFrequencyString);
		
		d40BluetoothPreference.setSummary(preferences.getString(D40_BLUETOOTH_NAME, null));
		ir21BluetoothPreference.setSummary(preferences.getString(IR21_BLUETOOTH_NAME, null));
		
		final String timeoutString = preferences.getString(TIMEOUT, "") + " " + getString(R.string.milliseconds_short);
		timeoutPreference.setSummary(timeoutString);
		
		d40BluetoothPreference.setOnPreferenceClickListener(this);
		ir21BluetoothPreference.setOnPreferenceClickListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }    
	
	@Override
	public void onPause() {
		super.onPause();
		
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		final Preference pref = getPreferenceScreen().findPreference(key);
		
		if (pref instanceof CheckBoxPreference) {
			return;
		} 
		
		final String preferenceValue = sharedPreferences.getString(key, "");
		if (key.equals(TIMEOUT) || key.equals(BROADCAST_FREQUENCY)) {
			pref.setSummary(preferenceValue + " " + getString(R.string.milliseconds_short));
		} else {
			pref.setSummary(preferenceValue);
		}
	}

	/**
	 * Bluetooth device preference is clickable
	 */
	@Override
	public boolean onPreferenceClick(Preference preference) {
		
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        
		if (adapter == null) {
			Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_LONG);
			return true;
		}

		boolean d40orIr21 = preference.getKey().equals(D40_BLUETOOTH_NAME);
		
		if (!adapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

			startActivityForResult(enableBtIntent, 
					d40orIr21 
						? REQUEST_D40_ENABLE_BT
						: REQUEST_IR21_ENABLE_BT);
			
			return true;
		}
		
		findBluetoothSensor(d40orIr21 ? REQUEST_FIND_D40 : REQUEST_FIND_IR21);
		
		return true;
	}

	private void findBluetoothSensor(int requestCode) {		
		final Intent settings = new Intent(
				this,
				LeanBluetoothPairingInterestingDevices.class);
		settings.putExtra(
				LeanBluetoothPairingInterestingDevices.DRIVER_ACTION,
				requestCode == REQUEST_FIND_D40 ? D40CachedSink.ACTION : IR21Sink.ACTION);
		settings.putExtra(
				LeanBluetoothPairingInterestingDevices.INTERESTING_DEVICE_NAME_PREFIX,
				"taidoc");
		settings.putExtra(
				LeanBluetoothPairingInterestingDevices.DISCONNECT_WHEN_DONE, 
				true);
		
		startActivityForResult(settings, requestCode);
	}
	

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)  {    	
    	if (resultCode != Activity.RESULT_OK) {
    		return;
    	}
    		
    	switch(requestCode) {
    		case REQUEST_D40_ENABLE_BT:
    			findBluetoothSensor(REQUEST_FIND_D40);
    			break;
    			
    		case REQUEST_IR21_ENABLE_BT:
    			findBluetoothSensor(REQUEST_FIND_IR21);
    			break;
    			
    		case REQUEST_FIND_D40:
    		{
    			final SharedPreferences prefs = getSharedPreferences(APP_PREFERENCES_FILE, MODE_PRIVATE);
    			final Editor editor = prefs.edit();
    			
				final String address = data.getStringExtra(LeanBluetoothPairingInterestingDevices.AVAILABLE_DEVICE_ADDRESS);
				
				final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
				final String desc = String.format("%s (%s)", device.getName(), device.getAddress());
				
				final Preference bluetoothDevicePreference = findPreference(D40_BLUETOOTH_NAME);
				bluetoothDevicePreference.setSummary(desc);

				editor.putString(D40_BLUETOOTH_NAME, desc);
				editor.putString(D40_BLUETOOTH_ADDRESS, address);
				editor.commit();
				break;
    		}
    		
    		case REQUEST_FIND_IR21:
    		{
    			final SharedPreferences prefs = getSharedPreferences(APP_PREFERENCES_FILE, MODE_PRIVATE);
    			final Editor editor = prefs.edit();
    			
				final String address = data.getStringExtra(LeanBluetoothPairingInterestingDevices.AVAILABLE_DEVICE_ADDRESS);
				
				final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
				final String desc = String.format("%s (%s)", device.getName(), device.getAddress());
				
				final Preference bluetoothDevicePreference = findPreference(IR21_BLUETOOTH_NAME);
				bluetoothDevicePreference.setSummary(desc);

				editor.putString(IR21_BLUETOOTH_NAME, desc);
				editor.putString(IR21_BLUETOOTH_ADDRESS, address);
				editor.commit();
				break;
    		}
    		
    		default:
    			break;
    	}
    }
}
