/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * Maksim Golivkin <maksim@golivkin.eu>
 ******************************************************************************/
package fi.hut.soberit.accelerometer;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import eu.mobileguild.WithHttpClient;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.UploadedType;
import fi.hut.soberit.sensors.generic.Uploader;
import fi.hut.soberit.sensors.services.UploaderService;

public class AccelerometerAxisUploader extends UploaderService {
	
	public static final String UPLOADER_ACTION = AccelerometerAxisUploader.class.getName() + ".ACTION";

	private Vector<UploadableRecord> recordsForUpload = new Vector<UploadableRecord>();
	
	private Handler handler;
	
	private String baseUrl;
	
	private UploadRecords updater = new UploadRecords();
	
	public static final String INTENT_AHL_URL = "ahl_url";

    public static final String INTENT_USERNAME = "username";

	public static final String INTENT_PASSWORD = "password";

	public static final String INTENT_WEBLET = "weblet";


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		int res = super.onStartCommand(intent, flags, startId);		

		if (intent == null) {
			return res;
		}
		
		handler = new Handler();
		
		final StringBuilder builder = new StringBuilder();
		
		builder.append(intent.getStringExtra(INTENT_AHL_URL));
		builder.append("?");
		builder.append("username=");
		builder.append(intent.getStringExtra(INTENT_USERNAME));
		builder.append("&");
		builder.append("password=");
		builder.append(intent.getStringExtra(INTENT_PASSWORD));
		builder.append("&");
		builder.append("weblet=");
		builder.append(intent.getStringExtra(INTENT_WEBLET));

		baseUrl = builder.toString();

		Log.d(TAG, baseUrl);

		handler.postDelayed(updater, UploadRecords.UPLOAD_FREQUENCY);

		return res;
	}
	
	
	protected boolean isMimeTypeInteresting(String mimeType) {
		return mimeType.equals(DriverInterface.TYPE_ACCELEROMETER);
	}
	
	@Override
	public void onDestroy() {
		if (handler != null) {
			handler.removeCallbacks(updater);
		}
		
		super.onDestroy();
	}
	

	@Override
	public void onReceiveObservations(List<Parcelable> observations) {
		float maxX = Float.MIN_VALUE;
		
		float maxY = Float.MIN_VALUE;
		float maxZ = Float.MIN_VALUE;
		
		for(Parcelable parcelable: observations) {
			GenericObservation observation = (GenericObservation) parcelable;
			
			float x = observation.getFloat(0);
			float y = observation.getFloat(4);
			float z = observation.getFloat(8);
			
			if (x > maxX) {
				maxX = x;
			} 
			if (y > maxY) {
				maxY = y;
			} 
			if (z > maxZ) {
				maxZ = z;
			}				
		}
		
		recordsForUpload.add(new UploadableRecord(System.currentTimeMillis(), maxX, maxY, maxZ));		
	}
	
	class UploadRecords implements Runnable {

		final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy%20HH:mm:ss");
		
		static final int UPLOAD_FREQUENCY = 5*1000;

		@Override
		public void run() {
			Log.d(TAG, "UploadRecords::run");
			
			try {
				
				HttpClient client = ((WithHttpClient)AccelerometerAxisUploader.this.getApplication()).getHttpClient();
				
				for(int i =recordsForUpload.size() -1; i>= 0;i--) {
					final UploadableRecord record = recordsForUpload.remove(i);
					
					final String url = String.format("%s&x=%f&y=%f&z=%f&time=%s",
							baseUrl,
							record.maxX,
							record.maxY, 
							record.maxZ, 
							format.format(new Date((long)record.time)));

					final HttpGet request = new HttpGet();
					request.setURI(new URI(url));
					
					final HttpResponse response = client.execute(request);
					response.getEntity().getContent().close();
					
					Log.d(TAG, "uploading: " + url + " result " + response.getStatusLine());					
				}				
				
				handler.postDelayed(updater, UPLOAD_FREQUENCY);

			} catch (URISyntaxException e) {

				e.printStackTrace();
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}			
		}		
	}
	
	public static class UploaderDiscover extends UploaderService.Discover {
		@Override
		public Uploader[] getUploaders(Context context) {
			final String url = fi.hut.soberit.accelerometer.AccelerometerAxisUploader.UPLOADER_ACTION;
			
			Uploader[] uploaders = new Uploader[1];
			
			uploaders[0] = new Uploader(
					context.getString(R.string.accelerometer_uploader),
					url
					);
			uploaders[0].setId(131030769200l);
			
			final UploadedType[] uploadedTypes = new UploadedType[] {
				new UploadedType(DriverInterface.TYPE_ACCELEROMETER, uploaders[0].getId()),
			};
			
			uploaders[0].setUploadedTypes(uploadedTypes);
			
			return uploaders;
		}
	}
}

class UploadableRecord {
	
	public UploadableRecord(long time, float maxX, float maxY, float maxZ) {
		this.time = time;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	long time;
	float maxX;
	float maxY;
	float maxZ;
}
