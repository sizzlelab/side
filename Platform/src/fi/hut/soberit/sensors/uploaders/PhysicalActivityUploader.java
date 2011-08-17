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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import eu.mobileguild.WithHttpClient;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.UploadedType;
import fi.hut.soberit.sensors.generic.Uploader;
import fi.hut.soberit.sensors.services.UploaderService;

public class PhysicalActivityUploader extends PlaygroundUploader {

	public static final String TAG = PhysicalActivityUploader.class.getSimpleName();
	

	public static final String UPLOADER_ACTION = PhysicalActivityUploader.class.getName() + ".ACTION";
	public static final long SAFE_DELAY = 2*60*1000;
	
	private Hashtable<Long, UploadableRecord> minutesStatistics = new Hashtable<Long, UploadableRecord>();
	
	private Handler handler;
	
	
	private UploadRecords updater = new UploadRecords();
	private ObservationType pulseType;
	private ObservationType accelerationType;

	
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		int res = super.onStartCommand(intent, flags, startId);		

		if (intent == null) {
			return res;
		}

		pulseType = types.get(DriverInterface.TYPE_PULSE);
		accelerationType = types.get(DriverInterface.TYPE_ACCELEROMETER);
		
		handler = new Handler();
		
		handler.postDelayed(updater, UploadRecords.UPLOAD_FREQUENCY);

		return res;
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


		synchronized(minutesStatistics) {
			for(Parcelable parcelable: observations) {

				final GenericObservation observation = (GenericObservation) parcelable;
				
				long minutesIndex = observation.getTime() / (60 * 1000);
				UploadableRecord uploadableRecord = minutesStatistics.get(minutesIndex);
				if (uploadableRecord == null) {
					uploadableRecord = new UploadableRecord(minutesIndex);
					minutesStatistics.put(minutesIndex, uploadableRecord);
				}
				
				if (observation.getObservationTypeId() == pulseType.getId()) {
					final int pulse = observation.getInteger(0);
					uploadableRecord.updateWithHeartBeat(pulse);
					
				} else if (observation.getObservationTypeId() == accelerationType.getId()) {
					float x = observation.getFloat(0);
					float y = observation.getFloat(4);
					float z = observation.getFloat(8);
					
					uploadableRecord.updateWithAcceleration(x, y, z);
				}			
			}
		}

	}
	
	public static class UploaderDiscover extends UploaderService.Discover {
		@Override
		public Uploader[] getUploaders(Context context) {
			final String url = PhysicalActivityUploader.UPLOADER_ACTION;
			
			Uploader[] uploaders = new Uploader[1];
			
			uploaders[0] = new Uploader(
					context.getString(R.string.physical_activity_uploader),
					url
					);
			uploaders[0].setId(1310642932000l);
			
			final UploadedType[] uploadedTypes = new UploadedType[] {
				new UploadedType(DriverInterface.TYPE_PULSE, uploaders[0].getId()),
				new UploadedType(DriverInterface.TYPE_ACCELEROMETER, uploaders[0].getId()),
			};
			
			uploaders[0].setUploadedTypes(uploadedTypes);
			
			return uploaders;
		}
	}
	
	class UploadRecords implements Runnable {

		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");
		
		static final int UPLOAD_FREQUENCY = 30*1000;

		@Override
		public void run() {
			Log.d(TAG, "UploadRecords::run");
			
			try {
				
				long currentMinuteIndex = (System.currentTimeMillis() - SAFE_DELAY)/ (60 * 1000);
				
				Log.d(TAG, "records: "+ minutesStatistics.size());
				UploadableRecord record = null;
				Set<Long> indices = minutesStatistics.keySet();
				for (Long minuteIndex : indices) {

					if (currentMinuteIndex <= minuteIndex) {
						continue;
					}
					record = minutesStatistics.get(minuteIndex);
				}					
				
				if (record == null) {
					return;
				}
					
				final Date time = new Date((long)record.minuteIndex * (60 * 1000));
				boolean success = makeRequest(String.format("avgAcceleration=%f&minPulse=%d&avgPulse=%d&maxPulse=%d&time=%s",
						record.getAvgAcceleration(),
						record.minPulse, 
						record.getAvgPulse(),
						record.maxPulse,
						format.format(time)));
				
				if (success) {
					minutesStatistics.remove(record.minuteIndex);
				}
			
			} catch (URISyntaxException e) {

				Log.d(TAG, "", e);
			} catch (ClientProtocolException e) {

				Log.d(TAG, "", e);
			} catch (IOException e) {
				Log.d(TAG, "", e);
				
			} finally {
				handler.postDelayed(updater, UPLOAD_FREQUENCY);
			}
		}		
	}

}

class UploadableRecord {
	
	public UploadableRecord(long minuteIndex) {
		this.minuteIndex = minuteIndex;
	}

	long minuteIndex;
	float accelerationSum;
	int accelerationCounter = 0;
	
	int minPulse = Integer.MAX_VALUE;
	int pulseSum;
	int maxPulse = Integer.MIN_VALUE;
	
	int pulseCounter;
	
	boolean uploaded = false;
	
	public void updateWithAcceleration(float x, float y, float z) {
		accelerationSum += Math.sqrt(x*x + y*y + z*z) - SensorManager.GRAVITY_EARTH;
		
		accelerationCounter++;
	}
	
	public void updateWithHeartBeat(int pulse) {
		pulseSum +=pulse;
		
		minPulse = Math.min(pulse, minPulse);
		maxPulse = Math.max(pulse, maxPulse);
		
		pulseCounter++;
	}

	public boolean isUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
	
	
	public double getAvgAcceleration() {

		return accelerationSum / accelerationCounter;
	}

	public int getAvgPulse() {
		if (pulseCounter == 0) {
			return 0;
		}
		
		return Math.round(pulseSum / pulseCounter);
	}
}
