package fi.hut.soberit.sensors.hxm;


import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

public class HxMPulseApplication extends Application {

	private static final String TAG = HxMPulseApplication.class.getSimpleName();

	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		PreferenceManager.setDefaultValues(
				this, 
				HxMPulseSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE,
				R.xml.preferences, 
				false);
	}
}
