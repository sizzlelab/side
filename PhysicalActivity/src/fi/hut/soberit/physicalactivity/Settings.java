/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.physicalactivity;

import eu.mobileguild.bluetooth.BluetoothPairingActivity;
import eu.mobileguild.ui.ForbiddenEmptyPreferenceValidation;
import eu.mobileguild.utils.SettingUtils;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	private static final String TAG = Settings.class.getSimpleName();
	
	public static final String ACTIVITY_SESSION_IN_PROCESS = "activity.sessionId";
	
	public static final String VITAL_SESSION_IN_PROCESS = "vital.sessionId";
	
	public static final String APP_PREFERENCES_FILE = "settings";

	public static final String SIDE_UPLOAD_PROCESS_WORKING = "side upload process working";

	public static final String RECORDING_FREQUENCY = "accelerometer.recording_frequency";
	public static final String BROADCAST_FREQUENCY = "accelerometer.broadcast_frequency";
	
	public static final String HXM_BLUETOOTH_NAME = "hxm.bluetooth_device";
	public static final String HXM_BLUETOOTH_ADDRESS = "hxm.evice_address";

	public static final String D40_BLUETOOTH_NAME = "forad40.bluetooth_device";
	public static final String D40_BLUETOOTH_ADDRESS = "forad40.evice_address";

<<<<<<< HEAD
	public static final String IR21_BLUETOOTH_NAME = "forair21.bluetooth_device";
	public static final String IR21_BLUETOOTH_ADDRESS = "forair21.evice_address";
=======
	public static final String TIMEOUT = "timeout";
>>>>>>> 6bbdb6bde14a718a7056bac35b9cfc79dc6c6dd5

	
<<<<<<< HEAD
	public static final String TIMEOUT = "timeout";	
	
	private static final int REQUEST_ENABLE_BT_FOR_HXM = 13;
	private static final int REQUEST_ENABLE_BT_FOR_D40 = 12;
	private static final int REQUEST_ENABLE_BT_FOR_IR21 = 14;
	
	private static final int REQUEST_FIND_HXM = 20;
	private static final int REQUEST_FIND_D40 = 21;
	private static final int REQUEST_FIND_IR21 = 22;

	public static final String VITAL_USERNAME = "vital.username";
	public static final String VITAL_PASSWORD = "vital.password";
	public static final String BLOOD_PRESSURE_WEBLET = "vital.blood_pressure.weblet";
	public static final String GLUCOSE_WEBLET = "vital.glucose.weblet";
=======
	private static final int REQUEST_ENABLE_BT_FOR_HXM = 13;
	private static final int REQUEST_ENABLE_BT_FOR_FORA = 12;
>>>>>>> 6bbdb6bde14a718a7056bac35b9cfc79dc6c6dd5
	
	public static final String SIDE_URL = "side.url";
	public static final String SIDE_USERNAME = "side.username";
	public static final String SIDE_PASSWORD = "side.password";
	public static final String SIDE_PROJECT_CODE = "side.project_code";

	public static final String METER = "meter";

	public static final String METER_HXM = "hxm";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final PreferenceManager preferenceManager = getPreferenceManager(); 
		preferenceManager.setSharedPreferencesName(Settings.APP_PREFERENCES_FILE);
		addPreferencesFromResource(R.xml.preferences);
		
  		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
  		final SharedPreferences preferences = preferenceManager.getSharedPreferences();

		final Preference thresholdPreference = findPreference(RECORDING_FREQUENCY);
		final Preference acknowledgementPreference = findPreference(BROADCAST_FREQUENCY);		
		final Preference meterPreference = findPreference(METER);
		
		final Preference sideUrlPreference = findPreference(SIDE_URL);		
		final Preference sideUsernamePreference = findPreference(SIDE_USERNAME);
		final Preference sidePasswordPreference = findPreference(SIDE_PASSWORD);
		final Preference sideProjectCodePreference = findPreference(SIDE_PROJECT_CODE);		
		
		final Preference hxmBluetoothNamePreference = findPreference(HXM_BLUETOOTH_NAME);
		final Preference timeoutPreference = findPreference(TIMEOUT);
		
		final Preference d40BluetoothNamePreference = findPreference(D40_BLUETOOTH_NAME);
		
		final Preference ir21BluetoothNamePreference = findPreference(IR21_BLUETOOTH_NAME);

		
		Resources resources = getResources();
		
		final String[] meters = resources.getStringArray(R.array.meters);
		final String[] meterNames = resources.getStringArray(R.array.meter_names);

		meterPreference.setSummary(SettingUtils.getEntryName(meterNames, meters, getString(R.string.meter_default)));
		
		meterPreference.setEnabled(false);
		
		
		final String timeoutString = preferences.getString(TIMEOUT, "") + " " + getString(R.string.milliseconds_short);
		timeoutPreference.setSummary(timeoutString);
		
		hxmBluetoothNamePreference.setOnPreferenceClickListener(this);
		d40BluetoothNamePreference.setOnPreferenceClickListener(this);
		ir21BluetoothNamePreference.setOnPreferenceClickListener(this);
		
		final String hxmBtDeviceName = preferences.getString(HXM_BLUETOOTH_NAME, ""); 
		hxmBluetoothNamePreference.setSummary(hxmBtDeviceName.length() == 0 
				? preferences.getString(HXM_BLUETOOTH_ADDRESS, "")
				: hxmBtDeviceName);
		
		final String d40BtDeviceName = preferences.getString(D40_BLUETOOTH_NAME, ""); 
		d40BluetoothNamePreference.setSummary(d40BtDeviceName.length() == 0 
				? preferences.getString(D40_BLUETOOTH_ADDRESS, "")
				: d40BtDeviceName);

		final String ir21BtDeviceName = preferences.getString(IR21_BLUETOOTH_NAME, ""); 
		ir21BluetoothNamePreference.setSummary(ir21BtDeviceName.length() == 0 
				? preferences.getString(IR21_BLUETOOTH_ADDRESS, "")
				: ir21BtDeviceName);

		
		final String ms = getString(R.string.milliseconds_short);
		thresholdPreference.setSummary(preferences.getString(RECORDING_FREQUENCY, "")  + " " + ms);		
		acknowledgementPreference.setSummary(preferences.getString(BROADCAST_FREQUENCY, "") + " " + ms);
		
		sideUrlPreference.setSummary(preferences.getString(SIDE_URL, ""));
		sideUsernamePreference.setSummary(preferences.getString(SIDE_USERNAME, ""));
		sidePasswordPreference.setSummary(SettingUtils.getStarsForPassword(preferences.getString(SIDE_PASSWORD, "")));
		sideProjectCodePreference.setSummary(preferences.getString(SIDE_PROJECT_CODE, ""));

		
		final ForbiddenEmptyPreferenceValidation preferenceChangeListener = new ForbiddenEmptyPreferenceValidation(this);
		
		thresholdPreference.setOnPreferenceChangeListener(preferenceChangeListener);		

		acknowledgementPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		
		sideUrlPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		sideUsernamePreference.setOnPreferenceChangeListener(preferenceChangeListener);
		sidePasswordPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		sideProjectCodePreference.setOnPreferenceChangeListener(preferenceChangeListener);

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
		
		Log.d(TAG, key);
		if (pref instanceof CheckBoxPreference) {
			return;
		} else if (pref instanceof ListPreference) {
			final ListPreference langPref = (ListPreference) pref;
			
			pref.setSummary(langPref.getEntry());
		} else if(key.equals(RECORDING_FREQUENCY) || key.equals(BROADCAST_FREQUENCY) || key.equals(TIMEOUT)) {
			
			final String ms = getString(R.string.milliseconds_short);
			pref.setSummary(((EditTextPreference)pref).getText() + " " + ms);
		} else if (key.equals(SIDE_PASSWORD)){
			
			pref.setSummary(SettingUtils.getStarsForPassword(((EditTextPreference)pref).getText()));
		} else {
			pref.setSummary(((EditTextPreference)pref).getText());			
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
		
		if (!adapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_FOR_HXM);
			
			return true;
		}
		
		final String key = preference.getKey();
		if (key.equals(HXM_BLUETOOTH_NAME)) {
			findBtDevice(REQUEST_FIND_HXM);
			
		} else if (key.equals(D40_BLUETOOTH_NAME)) {
			findBtDevice(REQUEST_FIND_D40);
			
		} else {
			findBtDevice(REQUEST_FIND_IR21);
		}
		
		return true;
	}

	private void findBtDevice(int requestCode) {
		Intent dialogIntent = new Intent(this, BluetoothPairingActivity.class);
		startActivityForResult(dialogIntent, requestCode);
	}
	

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)  {    	
    	if (resultCode != Activity.RESULT_OK) {
    		return;
    	}
    		
    	switch(requestCode) {
    		case REQUEST_ENABLE_BT_FOR_HXM:
    			findBtDevice(REQUEST_FIND_HXM);
    			break;
    			
    		case REQUEST_ENABLE_BT_FOR_D40:
    			findBtDevice(REQUEST_FIND_D40);
    			break;

    		case REQUEST_ENABLE_BT_FOR_IR21:
    			findBtDevice(REQUEST_FIND_IR21);
    			break;

    			
    		case REQUEST_FIND_HXM:
    		{
    			final SharedPreferences prefs = getSharedPreferences(APP_PREFERENCES_FILE, MODE_PRIVATE);
    			final Editor editor = prefs.edit();
    			
				final String address = data.getStringExtra(BluetoothPairingActivity.INTENT_DEVICE_ADDRESS);
				
				final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
				final String desc = String.format("%s (%s)", device.getName(), device.getAddress());
				
				final Preference bluetoothDevicePreference = findPreference(HXM_BLUETOOTH_NAME);
				bluetoothDevicePreference.setSummary(desc);

				editor.putString(HXM_BLUETOOTH_NAME, desc);
				editor.putString(HXM_BLUETOOTH_ADDRESS, address);
				editor.commit();
				break;
    		}
    		case REQUEST_FIND_D40:
    		{
    			final SharedPreferences prefs = getSharedPreferences(APP_PREFERENCES_FILE, MODE_PRIVATE);
    			final Editor editor = prefs.edit();
    			
				final String address = data.getStringExtra(BluetoothPairingActivity.INTENT_DEVICE_ADDRESS);
				
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
    			
				final String address = data.getStringExtra(BluetoothPairingActivity.INTENT_DEVICE_ADDRESS);
				
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
