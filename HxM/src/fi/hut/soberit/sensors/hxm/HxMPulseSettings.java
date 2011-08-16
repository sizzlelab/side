package fi.hut.soberit.sensors.hxm;

import java.util.Locale;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;
import eu.mobileguild.ApplicationWithChangingLocale;
import eu.mobileguild.bluetooth.BluetoothPairingActivity;

public class HxMPulseSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	public static final String APP_PREFERENCES_FILE = "preferences";

	private static final String TAG = HxMPulseSettings.class.getSimpleName();
		
	public static final String BROADCAST_FREQUENCY = "broadcast_frequency";

	public static final String BLUETOOTH_DEVICE = "bluetooth_device";
	public static final String BLUETOOTH_DEVICE_ADDRESS = "device_address";
	
	public static final String TIMEOUT = "timeout";

	private static final int REQUEST_FIND_HXM = 14;
	private static final int REQUEST_ENABLE_BT = 13;
		
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final PreferenceManager preferenceManager = getPreferenceManager(); 
		preferenceManager.setSharedPreferencesName(APP_PREFERENCES_FILE);
		addPreferencesFromResource(R.xml.preferences);
		
  		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
  		final SharedPreferences preferences = preferenceManager.getSharedPreferences();

  		final Preference broadcastingFrequencyPreference = findPreference(BROADCAST_FREQUENCY);
		final Preference bluetoothDevicePreference = findPreference(BLUETOOTH_DEVICE);
		final Preference timeoutPreference = findPreference(TIMEOUT);

		final String broadcastFrequencyString = 
			preferences.getString(BROADCAST_FREQUENCY, "") 
				+ " " 
				+ getString(R.string.milliseconds_short);
		broadcastingFrequencyPreference.setSummary(broadcastFrequencyString);
		
		bluetoothDevicePreference.setSummary(preferences.getString(BLUETOOTH_DEVICE, ""));
		bluetoothDevicePreference.setSummary(preferences.getString(BLUETOOTH_DEVICE_ADDRESS, ""));
		
		final String timeoutString = preferences.getString(TIMEOUT, "") + " " + getString(R.string.milliseconds_short);
		timeoutPreference.setSummary(timeoutString);
		
		bluetoothDevicePreference.setOnPreferenceClickListener(this);
		
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