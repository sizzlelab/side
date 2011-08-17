/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.uploaders;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import eu.mobileguild.WithHttpClient;
import fi.hut.soberit.sensors.services.UploaderService;

public abstract class PlaygroundUploader extends UploaderService {

	public static final String INTENT_AHL_URL = "ahl_url";
	public static final String INTENT_USERNAME = "username";
	public static final String INTENT_PASSWORD = "password";
	public static final String INTENT_WEBLET = "weblet";
	
	private String baseUrl;
	private HttpClient client;

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		int res = super.onStartCommand(intent, flags, startId);		

		if (intent == null) {
			return res;
		}
		
		final StringBuilder builder = new StringBuilder();
		
		builder.append(intent.getStringExtra(INTENT_AHL_URL));
		builder.append("?");
		builder.append("username=");
		builder.append(intent.getStringExtra(INTENT_USERNAME));
		builder.append("&");
		builder.append("password=");
		builder.append(intent.getStringExtra(INTENT_PASSWORD));
		
		final String weblet = intent.getStringExtra(INTENT_WEBLET);
		if (weblet != null) {
			builder.append("&");
			builder.append("weblet=");
			builder.append(weblet);
		}
		
		baseUrl = builder.toString();
		
		Log.d(TAG, "baseUrl:" + baseUrl);
		client = ((WithHttpClient)getApplication()).getHttpClient();
		
		return res;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected boolean makeRequest(String prefix) throws ClientProtocolException, IOException, URISyntaxException {
		final String url = baseUrl + "&" + prefix;

		final HttpGet request = new HttpGet();
		request.setURI(new URI(url));

		final HttpResponse response = client.execute(request);
		response.getEntity().getContent().close();
		
		Log.d(TAG, "uploading: " + url + " result " + response.getStatusLine());
		
		return response.getStatusLine().getStatusCode() == 200;
	}

}
