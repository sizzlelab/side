package fi.hut.soberit.physicalactivity;

import android.preference.PreferenceManager;
import android.util.Log;
import eu.mobileguild.ApplicationProvidingHttpClient;
import eu.mobileguild.ApplicationWithGlobalPreferences;

public class PhysicalActivityApplication 
	extends ApplicationProvidingHttpClient 
	implements ApplicationWithGlobalPreferences {
	
	public static String TAG = PhysicalActivityApplication.class.getSimpleName();
	
	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		PreferenceManager.setDefaultValues(
				this, 
				Settings.APP_PREFERENCES_FILE,
				MODE_PRIVATE,
				R.xml.preferences, 
				false);

		super.onCreate();
	}

	@Override
	public String getPreferenceFileName() {
		return Settings.APP_PREFERENCES_FILE;
	}
}
