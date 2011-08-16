package fi.hut.soberite.accelerometer;

import android.preference.PreferenceManager;
import android.util.Log;
import eu.mobileguild.ApplicationProvidingHttpClient;

public class AccelerometerDriverApplication extends ApplicationProvidingHttpClient {
	
	public static String TAG = AccelerometerDriverApplication.class.getSimpleName();
	
	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		PreferenceManager.setDefaultValues(
				this, 
				AccelerometerDriverSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE,
				R.xml.preferences, 
				false);

		super.onCreate();
	}
}
