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
package fi.hut.soberit.physicalactivity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.uploaders.PlaygroundUploader;

public class ForaUploader extends PlaygroundUploader {
	
	public static final String ACTION = ForaUploader.class.getName();

	private static final String LAST_BLOOD_PRESSURE_TIMESTAMP = "blood pressure timestamp";
	private static final String LAST_GLUCOSE_TIMESTAMP = "glucose timestamp";
	
	public static final String INTENT_GLUCOSE_WEBLET = "glucose weblet";
	public static final String INTENT_BLOOD_PRESSURE_WEBLET = "blood pressure weblet";

	private Handler handler;
	
	private UploadRecords uploadRecords = null;
	private ObservationType glucoseType;
	private ObservationType bloodPressureType;

	private Vector<GenericObservation> queue = new Vector<GenericObservation>();
		
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		int res = super.onStartCommand(intent, flags, startId);		

		if (intent == null) {
			return res;
		}

		glucoseType = types.get(DriverInterface.TYPE_GLUCOSE);
		bloodPressureType = types.get(DriverInterface.TYPE_BLOOD_PRESSURE);
		
		handler = new Handler();
		
		final String glucoseWeblet = intent.getStringExtra(INTENT_GLUCOSE_WEBLET);
		final String bloodPressureWeblet = intent.getStringExtra(INTENT_BLOOD_PRESSURE_WEBLET);
		
		Log.d(TAG, "Glucose Weblet: " + glucoseWeblet + ", Blood Pressure: " + bloodPressureWeblet);
		
		uploadRecords = new UploadRecords(queue, bloodPressureWeblet, glucoseWeblet);
		
		handler.postDelayed(uploadRecords, UploadRecords.UPLOAD_FREQUENCY);

		return res;
	}
	
	@Override
	public void onDestroy() {
		if (handler != null) {
			handler.removeCallbacks(uploadRecords);
		}
		
		super.onDestroy();
	}
	
	@Override
	public void onReceiveObservations(List<Parcelable> observations) {
		Log.d(TAG, "onReceiveObservations");
		
		SharedPreferences prefs = getSharedPreferences(Settings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		
		Log.d(TAG, "LAST_GLUCOSE_TIMESTAMP: " + prefs.getLong(LAST_GLUCOSE_TIMESTAMP, 0));
		Log.d(TAG, "LAST_BLOOD_PRESSURE_TIMESTAMP: " + prefs.getLong(LAST_BLOOD_PRESSURE_TIMESTAMP, 0));
		
		for(Parcelable p: observations) {
			final GenericObservation observation = (GenericObservation)p;
			
			Editor editor = prefs.edit(); 			
			long time = observation.getTime();
			
			if (observation.getObservationTypeId() == glucoseType.getId() &&
				prefs.getLong(LAST_GLUCOSE_TIMESTAMP, 0) != time) {
			
				editor.putLong(LAST_GLUCOSE_TIMESTAMP, time);
				editor.commit();
				
				queue.add(0, observation);
			} else if (observation.getObservationTypeId() == bloodPressureType.getId() &&
					prefs.getLong(LAST_BLOOD_PRESSURE_TIMESTAMP, 0) != time) {
				
				editor.putLong(LAST_BLOOD_PRESSURE_TIMESTAMP, time);
				editor.commit();
			
				queue.add(0, observation);
			} 
			
		}
	}
	
	class UploadRecords implements Runnable {

		public static final long UPLOAD_FREQUENCY = 5*1000;
		private Vector<GenericObservation> queue;
		private String bloodPressureWeblet;
		private String glucoseWeblet;


		public UploadRecords(Vector<GenericObservation> queue, String bloodPressureWeblet, String glucoseWeblet ) {
			this.queue = queue;
			
			this.bloodPressureWeblet = bloodPressureWeblet;
			this.glucoseWeblet = glucoseWeblet;
		}
		
		@Override
		public void run() {
			
			if (queue.size() ==  0) {
				handler.postDelayed(this, UPLOAD_FREQUENCY);
				return;
			}

			synchronized (queue) {
				try {
					for (int i = queue.size() -1; i>= 0; i--) {
						
						GenericObservation observation = queue.get(i);
						
						String url = null;
						
						if (observation.getObservationTypeId() == glucoseType.getId()) {
							url = String.format(
									"weblet=%s&glucose_level2=%.2f",
									URLEncoder.encode(glucoseWeblet, "UTF-8"),
									(observation.getInteger(0)* (float)0.05556));
						} else {
							url = String.format(
									"weblet=%s&SystolicPressure=%d&DiastolicPressure=%d&Pulse=0",
									URLEncoder.encode(bloodPressureWeblet, "UTF-8"),
									observation.getInteger(0),
									observation.getInteger(4));
						}
						

						boolean success = makeRequest(url);
						
						if (success) {
							queue.remove(i);
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "", e);
				} finally {
					handler.postDelayed(this, UPLOAD_FREQUENCY);
				}
				
			}
		}
		
	}

}
