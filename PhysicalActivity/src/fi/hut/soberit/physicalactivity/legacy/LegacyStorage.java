/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.physicalactivity.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import eu.mobileguild.utils.DataTypes;
import eu.mobileguild.utils.ThreadUtil;
import fi.hut.soberit.antpulse.AntPulseDriver;
import fi.hut.soberit.fora.ForaDriver;
import fi.hut.soberit.physicalactivity.AntPlusDriver;
import fi.hut.soberit.physicalactivity.Settings;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationValueDao;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.hxm.HxMDriver;
import fi.hut.soberit.sensors.services.StorageService;
import fi.hut.soberit.sensors.storage.GenericObservationStorage;

public class LegacyStorage extends StorageService {

	private static final int BUFFER_SIZE = 1;

	public static String ACTION = LegacyStorage.class.getName();

	public static final Storage STORAGE = new Storage(1311060163999l, ACTION);
	
	private Vector<LegacyObservation> loggingQueue = new Vector<LegacyObservation>();	
	
	public long sessionId;
	
	protected LoggingThread loggingThread;
	
	protected LegacyDatabaseHelper dbHelper;

	public ObservationValueDao observationDao;

	private HashMap<Long, ObservationType> typesIndex = new HashMap<Long, ObservationType>();
	
	private static final String LAST_BLOOD_PRESSURE_TIMESTAMP = "storage blood pressure timestamp";
	private static final String LAST_GLUCOSE_TIMESTAMP = "storage glucose timestamp";

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
		int res = super.onStartCommand(intent, flags, startId);
		if (intent == null) {
			return res;
		}

		sessionId = intent.getLongExtra(DriverInterface.INTENT_SESSION_ID, -1);

		Log.d(TAG, "Server id: " + sessionId);
		
		dbHelper = new LegacyDatabaseHelper(this, sessionId);
		
		loggingThread = new LoggingThread(loggingQueue, BUFFER_SIZE, new LegacyObservationDao(dbHelper));
		loggingThread.start();
		
		for(ObservationType type : types.values()) {
			
			Log.d(TAG, "received types: " + type.getMimeType() + " " + type.getId());
			
			if (DriverInterface.TYPE_PULSE.equals(type.getMimeType())) {
				typesIndex.put(type.getId(), type);
			} else if (DriverInterface.TYPE_ACCELEROMETER.equals(type.getMimeType())) {
				typesIndex.put(type.getId(), type);
			} else if (DriverInterface.TYPE_GLUCOSE.equals(type.getMimeType())) {
				typesIndex.put(type.getId(), type);
			} else if (DriverInterface.TYPE_BLOOD_PRESSURE.equals(type.getMimeType())) {
				typesIndex.put(type.getId(), type);
			}
			
		}
		
		return res;
	}
		
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		if (loggingThread != null) {
			loggingThread.interrupt();
		}
	}
	
	@Override
	public void onReceiveObservations(List<Parcelable> observations) {
		
		final SharedPreferences prefs = getSharedPreferences(Settings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		Editor edit = prefs.edit();
		
		for(int i = observations.size() -1; i >= 0; i--) {

			final GenericObservation generic = (GenericObservation) observations.get(i);
			
			final ObservationType type = typesIndex.get(generic.getObservationTypeId());			
			
			Log.d(TAG, "type: " + generic.getObservationTypeId() + " " + null);
			if (type == null) {
				continue;
			}

			final LegacyObservation legacy = new LegacyObservation();
			
			legacy.setSessionId(sessionId);
			final long time = generic.getTime();
			
			if (DriverInterface.TYPE_PULSE.equals(type.getMimeType())) {
				legacy.setType(LegacyObservation.Type.PULSE);
				
				legacy.setObservation1(DataTypes.byteArrayToInt(generic.getValue(), 0));
				
			} else if (DriverInterface.TYPE_BLOOD_PRESSURE.equals(type.getMimeType())){
			
				if (prefs.getLong(LAST_BLOOD_PRESSURE_TIMESTAMP, 0) == time) {
					Log.d(TAG, "Same blood pressure record. Skipping");
					return;
				}
				edit.putLong(LAST_BLOOD_PRESSURE_TIMESTAMP, time);
				edit.commit();
				
				legacy.setType(LegacyObservation.Type.BLOOD_PRESSURE);
				
				legacy.setObservation1(DataTypes.byteArrayToInt(generic.getValue(), 0));
				legacy.setObservation2(DataTypes.byteArrayToInt(generic.getValue(), 4));
				
			} else if (DriverInterface.TYPE_GLUCOSE.equals(type.getMimeType())) {
				
				if (prefs.getLong(LAST_GLUCOSE_TIMESTAMP, 0) == time) {
					Log.d(TAG, "Same glucose record. Skipping");
					return;
				}
				edit.putLong(LAST_GLUCOSE_TIMESTAMP, time);
				edit.commit();
				
				legacy.setType(LegacyObservation.Type.GLUCOSE);
				
				final int glucoseValue = DataTypes.byteArrayToInt(generic.getValue(), 0);
				Log.d(TAG, "Glucose value: " + glucoseValue);
				legacy.setObservation1(glucoseValue);
				legacy.setObservation2(DataTypes.byteArrayToInt(generic.getValue(), 4));
				
			} else if (DriverInterface.TYPE_ACCELEROMETER.equals(type.getMimeType())) {
				legacy.setType(LegacyObservation.Type.ACCELERATION);
				
				legacy.setObservation1(DataTypes.byteArrayToFloat(generic.getValue(), 0));
				legacy.setObservation2(DataTypes.byteArrayToFloat(generic.getValue(), 4));
				legacy.setObservation2(DataTypes.byteArrayToFloat(generic.getValue(), 8));
			}
			
			legacy.setTime(LegacyDatabaseHelper.getUtcDateString(time));

			loggingQueue.add(legacy);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	class LoggingThread extends Thread {
		
		private Vector<LegacyObservation> queue;
		private int bufferSize;
		private LegacyObservationDao observationDao;

		public LoggingThread(Vector<LegacyObservation> queue, int bufferSize, LegacyObservationDao legacyObservationDao) {
			
			this.queue = queue;
			this.bufferSize = bufferSize;
			this.observationDao = legacyObservationDao;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "DatabaseInsertQueueThread::run");
			try {
				while(!isInterrupted()) {
					Log.d(TAG, "queue" + queue.size());
					
					while(queue.size() < bufferSize && !isInterrupted()) {
						Thread.sleep(1000);
					}
					
					ArrayList<LegacyObservation> copy = null;
					synchronized(queue) {
						int copySize = Math.min(10, queue.size());
						copy = new ArrayList<LegacyObservation>(copySize);
						for(int i = copySize -1; i >= 0; i--) {
							copy.add(0, queue.remove(i));
						}
					}
					
					while(copy.size() > 0) {
						ThreadUtil.throwIfInterruped();
								
						LegacyObservation observation = copy.remove(0);
						if (!saveObservation(observation)) {
							copy.add(0, observation);
						}
					}
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}			
		}

		protected boolean saveObservation(LegacyObservation observation) {	
			try {
				Log.d(TAG, "time: " + observation.getTime() + " type " + observation.getType());
				
				return observationDao.insert(observation) != -1;
			} catch(IllegalStateException e) {
				Log.d(TAG, "", e);
				return false;
			} 
		}
	}
	
	public static class Discover extends StorageService.Discover {
		public Storage[] getStorages(Context context) {
			Storage [] storages = new Storage[1];
			
			ArrayList<ObservationType> types = new ArrayList<ObservationType>();
			
			final AntPlusDriver.Discover antPlusDiscover = new AntPlusDriver.Discover();
			for(ObservationType type: antPlusDiscover.getObservationTypes(context)) {
				types.add(type);
			}

			final HxMDriver.Discover hxmDiscover = new HxMDriver.Discover();
			for(ObservationType type: hxmDiscover.getObservationTypes(context)) {
				types.add(type);
			}
			
			final ForaDriver.Discover foraDiscover = new ForaDriver.Discover();
			for(ObservationType type: foraDiscover.getObservationTypes(context)) {
				types.add(type);
			}

			
			storages[0] = new Storage(1311060161999l, ACTION);
			storages[0].setTypes(types);
			
			return storages;
		}
	}
}