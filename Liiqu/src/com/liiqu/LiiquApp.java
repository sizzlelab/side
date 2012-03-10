package com.liiqu;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Application;
import android.util.Log;

import com.github.droidfu.http.BetterHttp;
import com.liiqu.util.ui.ImageHtmlLoader;

public class LiiquApp extends Application {

	private static final int TWO_WEEKS = 14*24*60;

	@Override
	public void onCreate() {
		super.onCreate();

		final DefaultHttpClient client = new LiiquHttpsClient(getApplicationContext());
		
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
