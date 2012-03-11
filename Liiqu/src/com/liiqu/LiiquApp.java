package com.liiqu;

import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.droidfu.http.BetterHttp;
import com.liiqu.util.ui.ImageHtmlLoader;

public class LiiquApp extends Application {

	public static final String TAG = LiiquApp.class.getSimpleName();
	
	public static final boolean DEBUG = true;
	
	private static final int TWO_WEEKS = 14*24*60;

	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		
		final DefaultHttpClient client = new LiiquSecureClient(getApplicationContext());
		
		BetterHttp.setHttpClient(client);
		BetterHttp.setDefaultHeader("Content-type", "application/json");

        ImageHtmlLoader.initialize(this, TWO_WEEKS);
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		
		ImageHtmlLoader.clearCache();
		
		final AbstractHttpClient httpClient = BetterHttp.getHttpClient();

		if (httpClient != null && httpClient.getConnectionManager() != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}
}
