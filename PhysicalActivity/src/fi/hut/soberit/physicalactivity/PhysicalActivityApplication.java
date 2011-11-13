/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.physicalactivity;

import org.apache.http.client.HttpClient;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;
import eu.mobileguild.ApplicationProvidingHttpClient;
import eu.mobileguild.ApplicationWithGlobalPreferences;
import eu.mobileguild.HttpClientDelegate;
import eu.mobileguild.WithHttpClient;
import fi.hut.soberit.physicalactivity.legacy.LegacyDatabaseHelper;

public class PhysicalActivityApplication extends Application
	implements ApplicationWithGlobalPreferences, WithHttpClient {
	
	public static String TAG = PhysicalActivityApplication.class.getSimpleName();

	HttpClientDelegate httpClientDelegate = new HttpClientDelegate();
	
	public PhysicalActivityApplication() {
		
		httpClientDelegate.createHttpClient();
	}

	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		PreferenceManager.setDefaultValues(
				this, 
				Settings.APP_PREFERENCES_FILE,
				MODE_PRIVATE,
				R.xml.preferences, 
				false);

		
		new DatabaseHelper(this).getWritableDatabase();
		
		super.onCreate();
	}

	@Override
	public String getPreferenceFileName() {
		return Settings.APP_PREFERENCES_FILE;
	}
	
		
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		httpClientDelegate.shutdownHttpClient();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		httpClientDelegate.shutdownHttpClient();
	}

	@Override
	public HttpClient getHttpClient() {
		// TODO Auto-generated method stub
		return httpClientDelegate.getHttpClient();
	}
}
