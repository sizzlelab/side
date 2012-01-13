package fi.hut.soberit.sensors.zephyr.harness;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

public class BioHarnessApplication extends Application {

	private static final String TAG = BioHarnessApplication.class.getSimpleName();

	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		PreferenceManager.setDefaultValues(
				this, 
				BioHarnessSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE,
				R.xml.preferences, 
				false);
	}
}
