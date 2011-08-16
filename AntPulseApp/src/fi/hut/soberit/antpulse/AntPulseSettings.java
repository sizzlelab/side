package fi.hut.soberit.antpulse;

import eu.mobileguild.ui.ForbiddenEmptyPreferenceValidation;
import fi.hut.soberit.antpulse.R;
import android.app.Activity;
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

public class AntPulseSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    
	public static final String APP_PREFERENCES_FILE = "accDriver.settings";

	public static final String WEBLET = "accelerometer.weblet";
	
	public static final String AHL_URL = "accelerometer.ahl_url";

	public static final String USERNAME = "accelerometer.username";

	public static final String PASSWORD = "accelerometer.password";

	public static final String BROADCAST_FREQUENCY = "accelerometer.broadcast_frequency";
	
	private static final String TAG = AntPulseSettings.class.getSimpleName();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final PreferenceManager preferenceManager = getPreferenceManager(); 
		preferenceManager.setSharedPreferencesName(AntPulseSettings.APP_PREFERENCES_FILE);
		addPreferencesFromResource(R.xml.preferences);
		
  		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
  		final SharedPreferences preferences = preferenceManager.getSharedPreferences();

		final Preference acknowledgementPreference = findPreference(BROADCAST_FREQUENCY);		
		
		final Preference ahlUrlPreference = findPreference(AHL_URL);		
		final Preference webletPreference = findPreference(WEBLET);		
		final Preference usernamePreference = findPreference(USERNAME);		
		final Preference passwordPreference = findPreference(PASSWORD);		

		
		final String ms = getString(R.string.milliseconds_short);
		acknowledgementPreference.setSummary(preferences.getString(BROADCAST_FREQUENCY, "") + " " + ms);
		
		ahlUrlPreference.setSummary(preferences.getString(AHL_URL, ""));
		webletPreference.setSummary(preferences.getString(WEBLET, ""));
		usernamePreference.setSummary(preferences.getString(USERNAME, ""));
		passwordPreference.setSummary(preferences.getString(PASSWORD, ""));


		final ForbiddenEmptyPreferenceValidation preferenceChangeListener = new ForbiddenEmptyPreferenceValidation(this);
		
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
		} else if(key.equals(BROADCAST_FREQUENCY)) {
			
			final String ms = getString(R.string.milliseconds_short);
			pref.setSummary(((EditTextPreference)pref).getText() + " " + ms);
		} else {
			pref.setSummary(((EditTextPreference)pref).getText());
		}
	}
}