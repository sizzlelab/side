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
package fi.hut.soberite.accelerometer;


import android.content.Context;
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
import eu.mobileguild.ui.ForbiddenEmptyPreferenceValidation;

public class AccelerometerDriverSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
		
	public static final String APP_PREFERENCES_FILE = "accDriver.settings";

	public static final String WEBLET = "accelerometer.weblet";
	
	public static final String AHL_URL = "accelerometer.ahl_url";

	public static final String USERNAME = "accelerometer.username";

	public static final String PASSWORD = "accelerometer.password";

	public static final String RECORDING_DELAY = "accelerometer.recording_delay";
	public static final String RECORDING_FREQUENCY = "accelerometer.recording_frequency";
	public static final String BROADCAST_FREQUENCY = "accelerometer.broadcast_frequency";
	
	private static final String TAG = AccelerometerDriverSettings.class.getSimpleName();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final PreferenceManager preferenceManager = getPreferenceManager(); 
		preferenceManager.setSharedPreferencesName(AccelerometerDriverSettings.APP_PREFERENCES_FILE);
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

		
		final String ms = getString(R.string.milliseconds_short);
		delayPreference.setSummary(preferences.getString(RECORDING_DELAY, ""));		
		thresholdPreference.setSummary(preferences.getString(RECORDING_FREQUENCY, "")  + " " + ms);		
		acknowledgementPreference.setSummary(preferences.getString(BROADCAST_FREQUENCY, "") + " " + ms);
		
		ahlUrlPreference.setSummary(preferences.getString(AHL_URL, ""));
		webletPreference.setSummary(preferences.getString(WEBLET, ""));
		usernamePreference.setSummary(preferences.getString(USERNAME, ""));
		passwordPreference.setSummary(preferences.getString(PASSWORD, ""));


		final ForbiddenEmptyPreferenceValidation preferenceChangeListener = new ForbiddenEmptyPreferenceValidation(this);
		
		thresholdPreference.setOnPreferenceChangeListener(preferenceChangeListener);		
		delayPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		acknowledgementPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		
		ahlUrlPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		webletPreference.setOnPreferenceChangeListener(preferenceChangeListener);
		usernamePreference.setOnPreferenceChangeListener(preferenceChangeListener);
		passwordPreference.setOnPreferenceChangeListener(preferenceChangeListener);
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
		} else if(key.equals(RECORDING_FREQUENCY) || key.equals(BROADCAST_FREQUENCY)) {
			
			final String ms = getString(R.string.milliseconds_short);
			pref.setSummary(((EditTextPreference)pref).getText() + " " + ms);
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
}
