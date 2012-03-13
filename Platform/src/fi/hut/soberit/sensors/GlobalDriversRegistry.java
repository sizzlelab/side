package fi.hut.soberit.sensors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class GlobalDriversRegistry {
	
	public static final String TAG = GlobalDriversRegistry.class.getSimpleName();
	
	private static final String ID_PREFIX = ".ID";
	private static final String CONTEXT_PREFIX = ".CONTEXT";	
	
	private static final String PREFS_FILE = "drivers.prefs";
	
	private static Context appContext;
	
	public static void init(Context context) {
		appContext = context;
	}
	
	public static void startSession(Context context, Intent intent, long id) {
		
		final String driverAction = intent.getAction();
		
		Log.d(TAG, "startSession " + driverAction + " for " + context.getClass().getSimpleName());
		
		if (driverAction == null) {
			throw new IllegalArgumentException("Please, use the Intent with an Action set.");
		}
		
		final SharedPreferences prefs = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
				
		Editor edit = prefs.edit();
		edit.putLong(driverAction + ID_PREFIX, id);
		edit.putString(driverAction + CONTEXT_PREFIX, context.getClass().getName());
		edit.commit();
		
	
		context.startService(intent);
	}
	
	public static boolean isStarted(String driverAction) {
		
		boolean result = getSessionId(driverAction, Long.MIN_VALUE) != Long.MIN_VALUE;
		
		Log.d(TAG, "isStarted " + result);
		
		return result;
	}
	
	public static long getSessionId(String driverAction, long defaultId) {
		final SharedPreferences prefs = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

		return prefs.getLong(driverAction + ID_PREFIX, defaultId);
	}

	public static void startSession(
			Context context,
			Intent intent) {
		startSession(context, intent, System.currentTimeMillis());
	}
}
