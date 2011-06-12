package fi.hut.soberite.accelerometer;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

public class AccelerometerDriverApplication extends Application {
	
	public static String TAG = AccelerometerDriverApplication.class.getSimpleName();
	
	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		PreferenceManager.setDefaultValues(
				this, 
				AccelerometerDriver.APP_PREFERENCES_FILE,
				MODE_PRIVATE,
				R.xml.preferences, 
				false);

		super.onCreate();
	}
}
