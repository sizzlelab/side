package fi.hut.soberit.physicalactivity;

import eu.mobileguild.bluetooth.BluetoothPairingActivity;
import eu.mobileguild.ui.ForbiddenEmptyPreferenceValidation;
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

	public static final String WEBLET = "accelerometer.weblet";
	
	public static final String AHL_URL = "accelerometer.ahl_url";

	public static final String USERNAME = "accelerometer.username";

	public static final String PASSWORD = "accelerometer.password";

	public static final String RECORDING_DELAY = "accelerometer.recording_delay";
	public static final String RECORDING_FREQUENCY = "accelerometer.recording_frequency";
	public static final String BROADCAST_FREQUENCY = "accelerometer.broadcast_frequency";
	
	public static final String BLUETOOTH_DEVICE = "bluetooth_device";
	public static final String BLUETOOTH_DEVICE_ADDRESS = "device_address";
	
	public static final String TIMEOUT = "timeout";

	private static final int REQUEST_FIND_HXM = 14;
	private static final int REQUEST_ENABLE_BT = 13;

	public static final String VITAL_USERNAME = "vital.username";
	public static final String VITAL_PASSWORD = "vital.password";
	public static final String BLOOD_PRESSURE_WEBLET = "vital.blood_pressure.weblet";
	public static final String GLUCOSE_WEBLET = "vital.glucose.weblet";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final PreferenceManager preferenceManager = getPreferenceManager(); 
		preferenceManager.setSharedPreferencesName(Settings.APP_PREFERENCES_FILE);
		addPreferencesFromResource(R.xml.preferences);
		
  		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
  		final SharedPreferences preferences = preferenceManager.getSharedPreferences();

  		final Preference delayPreference = findPreference(RECORDING_DELAY);
		final Preference thresholdPreference = findPreference(RECORDING_FREQUENCY);
		final Preference acknowledgementPreference = findPreference(BROADCAST_FREQUENCY);		
		
		final Preference ahlUrlPreference = findPreference(AHL_URL);		
		final Preference webletPreference = findPreference(WEBLET);		
		final Preference usernamePreference = findPreference(USERNAME);		
		final Preference passwordPreference = findPreference(PASSWORD);		

		final Preference bloodPressureWebletPreference = findPreference(BLOOD_PRESSURE_WEBLET);		
		final Preference glucoseWebletPreference = findPreference(GLUCOSE_WEBLET);		

		final Preference vitalUsernamePreference = findPreference(VITAL_USERNAME);		
		final Preference vitalPasswordPreference = findPreference(VITAL_PASSWORD);
		
		final Preference bluetoothDevicePreference = findPreference(BLUETOOTH_DEVICE);
		final Preference timeoutPreference = findPreference(TIMEOUT);
		
		final String timeoutString = preferences.getString(TIMEOUT, "") + " " + getString(R.string.milliseconds_short);
		timeoutPreference.setSummary(timeoutString);
		
		bluetoothDevicePreference.setOnPreferenceClickListener(this);

		final String btDeviceName = preferences.getString(BLUETOOTH_DEVICE, ""); 
		bluetoothDevicePreference.setSummary(btDeviceName.length() == 0 
				? preferences.getString(BLUETOOTH_DEVICE_ADDRESS, "")
				: btDeviceName);
		
		final String ms = getString(R.string.milliseconds_short);
		delayPreference.setSummary(preferences.getString(RECORDING_DELAY, ""));		
		thresholdPreference.setSummary(preferences.getString(RECORDING_FREQUENCY, "")  + " " + ms);		
		acknowledgementPreference.setSummary(preferences.getString(BROADCAST_FREQUENCY, "") + " " + ms);
		
		ahlUrlPreference.setSummary(preferences.getString(AHL_URL, ""));
		webletPreference.setSummary(preferences.getString(WEBLET, ""));
		usernamePreference.setSummary(preferences.getString(USERNAME, ""));
		passwordPreference.setSummary(preferences.getString(PASSWORD, ""));

		vitalUsernamePreference.setSummary(preferences.getString(VITAL_USERNAME, ""));
		vitalPasswordPreference.setSummary(preferences.getString(VITAL_PASSWORD, ""));

		bloodPressureWebletPreference.setSummary(preferences.getString(BLOOD_PRESSURE_WEBLET, ""));
		glucoseWebletPreference.setSummary(preferences.getString(GLUCOSE_WEBLET, ""));

		
		final ForbiddenEmptyPreferenceValidation preferenceChangeListener = new ForbiddenEmptyPreferenceValidation(this);
		
		thresholdPreference.setOnPreferenceChangeListener(preferenceChangeListener);		
		delayPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		acknowledgementPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		
		ahlUrlPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		webletPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		usernamePreference.setOnPreferenceChangeListener(preferenceChangeListener);
		passwordPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		
		vitalUsernamePreference.setOnPreferenceChangeListener(preferenceChangeListener);
		vitalPasswordPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		
		bloodPressureWebletPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		glucoseWebletPreference.setOnPreferenceChangeListener(preferenceChangeListener);
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
		} else {
			pref.setSummary(((EditTextPreference)pref).getText());			
		}
		
	}
	
	
	public static int stringDelayToConstant(Context context, String delay) {
		
		final Resources res = context.getResources();
		
		final String[] values = res.getStringArray(R.array.recording_delays);
		if (delay.equals(values[0])) {
			return SensorManager.SENSOR_DELAY_FASTEST;
		} else if (delay.equals(values[1])) {
			return SensorManager.SENSOR_DELAY_GAME;
		} else if (delay.equals(values[2])) {
			return SensorManager.SENSOR_DELAY_NORMAL;
		} else if (delay.equals(values[3])) {
			return SensorManager.SENSOR_DELAY_UI;
		}
		
		return -1;
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
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			
			return true;
		}
		
		findHxMDevice();
		
		return true;
	}

	private void findHxMDevice() {
		Intent dialogIntent = new Intent(this, BluetoothPairingActivity.class);
		startActivityForResult(dialogIntent, REQUEST_FIND_HXM);
	}
	

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)  {    	
    	if (resultCode != Activity.RESULT_OK) {
    		return;
    	}
    		
    	switch(requestCode) {
    		case REQUEST_ENABLE_BT:
    			findHxMDevice();
    			break;
    			
    		case REQUEST_FIND_HXM:
    			final SharedPreferences prefs = getSharedPreferences(APP_PREFERENCES_FILE, MODE_PRIVATE);
    			final Editor editor = prefs.edit();
    			
				final String address = data.getStringExtra(BluetoothPairingActivity.INTENT_DEVICE_ADDRESS);
				
				final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
				final String desc = String.format("%s (%s)", device.getName(), device.getAddress());
				
				final Preference bluetoothDevicePreference = findPreference(BLUETOOTH_DEVICE);
				bluetoothDevicePreference.setSummary(desc);

				editor.putString(BLUETOOTH_DEVICE, desc);
				editor.putString(BLUETOOTH_DEVICE_ADDRESS, address);
				editor.commit();
				break;
    		default:
    			break;
    	}
    }

}
