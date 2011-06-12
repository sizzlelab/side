package fi.hut.soberite.accelerometer;


import java.util.Locale;

import eu.mobileguild.ApplicationWithChangingLocale;
import eu.mobileguild.ui.BluetoothPairingActivity;
import eu.mobileguild.ui.ForbiddenEmptyPreferenceValidation;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class AccelerometerDriverSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
		
	public static final String RECORDING_DELAY = "accelerometer.recording_delay";
	public static final String RECORDING_FREQUENCY = "accelerometer.recording_frequency";
	public static final String BROADCAST_FREQUENCY = "accelerometer.broadcast_frequency";
	
	private static final String TAG = AccelerometerDriverSettings.class.getSimpleName();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final PreferenceManager preferenceManager = getPreferenceManager(); 
		preferenceManager.setSharedPreferencesName(AccelerometerDriver.APP_PREFERENCES_FILE);
		addPreferencesFromResource(R.xml.preferences);
		
  		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
  		final SharedPreferences preferences = preferenceManager.getSharedPreferences();

  		final Preference delayPreference = findPreference(RECORDING_DELAY);
		final Preference thresholdPreference = findPreference(RECORDING_FREQUENCY);
		final Preference acknowledgementPreference = findPreference(BROADCAST_FREQUENCY);		
		
		final String ms = getString(R.string.milliseconds_short);
		delayPreference.setSummary(preferences.getString(RECORDING_DELAY, ""));		
		thresholdPreference.setSummary(preferences.getString(RECORDING_FREQUENCY, "")  + " " + ms);		
		acknowledgementPreference.setSummary(preferences.getString(BROADCAST_FREQUENCY, "") + " " + ms);
		
		final ForbiddenEmptyPreferenceValidation preferenceChangeListener = new ForbiddenEmptyPreferenceValidation(this);
		
		thresholdPreference.setOnPreferenceChangeListener(preferenceChangeListener);		
		delayPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		acknowledgementPreference.setOnPreferenceChangeListener(preferenceChangeListener);
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
		} else if (key.equals(RECORDING_DELAY)) {
			final ListPreference langPref = (ListPreference) pref;
			
			pref.setSummary(langPref.getEntry());
		} else if(key.equals(RECORDING_FREQUENCY) || key.equals(BROADCAST_FREQUENCY)) {
			
			final String ms = getString(R.string.milliseconds_short);
			pref.setSummary(((EditTextPreference)pref).getText() + " " + ms);
		}
	}
	
	
	public static int stringDelayToConstant(Context context, String delay) {
		
		final Resources res = context.getResources();
		
		final String[] values = res.getStringArray(R.array.recording_delay_values);
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
}
