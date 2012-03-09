package com.liiqu;

import org.apache.http.impl.client.AbstractHttpClient;

import android.app.Application;

import com.github.droidfu.http.BetterHttp;
import com.liiqu.util.ui.ImageHtmlLoader;

public class LiiquApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		BetterHttp.setupHttpClient();

        ImageHtmlLoader.initialize(this, 24*60*3);

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
