package fi.hut.soberit.antpulse;

import android.preference.PreferenceManager;
import android.util.Log;
import eu.mobileguild.ApplicationProvidingHttpClient;
import fi.hut.soberit.antpulse.R;


public class AntPulseDriverApplication extends ApplicationProvidingHttpClient {
	
	public static String TAG = AntPulseDriverApplication.class.getSimpleName();
	
	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		PreferenceManager.setDefaultValues(
				this, 
				AntPulseSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE,
				R.xml.preferences, 
				false);

		super.onCreate();
	}
}