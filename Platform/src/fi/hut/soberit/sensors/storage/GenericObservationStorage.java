package fi.hut.soberit.sensors.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import eu.mobileguild.utils.ThreadUtil;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationValueDao;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.services.StorageService;

public class GenericObservationStorage extends StorageService {

	private static final int BUFFER_SIZE = 10;

	public static String ACTION = GenericObservationStorage.class.getName();

	public static final Storage STORAGE = new Storage(1311060163999l, ACTION);
	
	private Vector<GenericObservation> loggingQueue = new Vector<GenericObservation>();	
	
	public long sessionId;
	
	protected LoggingThread loggingThread;
	
	protected DatabaseHelper dbHelper;

	public ObservationValueDao observationDao;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
		int res = super.onStartCommand(intent, flags, startId);
		if (intent == null) {
			return res;
		}

		sessionId = intent.getLongExtra(DriverInterface.INTENT_SESSION_ID, -1);

		dbHelper = new DatabaseHelper(this, sessionId);
		
		loggingThread = new LoggingThread(loggingQueue, BUFFER_SIZE, new ObservationValueDao(dbHelper));
		loggingThread.start();
		
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
		for(int i = observations.size() -1; i >= 0; i--) { 
			loggingQueue.add((GenericObservation) observations.get(i));
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	class LoggingThread extends Thread {
		
		private Vector<GenericObservation> queue;
		private int bufferSize;
		private ObservationValueDao observationDao;

		public LoggingThread(Vector<GenericObservation> queue, int bufferSize, ObservationValueDao observationDao) {
			
			this.queue = queue;
			this.bufferSize = bufferSize;
			this.observationDao = observationDao;
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
					
					ArrayList<GenericObservation> copy = null;
					synchronized(queue) {
						int copySize = Math.min(10, queue.size());
						copy = new ArrayList<GenericObservation>(copySize);
						for(int i = copySize -1; i >= 0; i--) {
							copy.add(0, queue.remove(i));
						}
					}
					
					while(copy.size() > 0) {
						ThreadUtil.throwIfInterruped();
								
						GenericObservation observation = copy.remove(0);
						if (!saveObservation(observation)) {
							copy.add(0, observation);
						}
					}
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}			
		}

		protected boolean saveObservation(GenericObservation observation) {	
			try {
				Log.d(TAG, "time: " + observation.getTime() + " type " + observation.getObservationTypeId());
				return observationDao.insertObservationValue(observation) != -1;
			} catch(IllegalStateException e) {
				Log.d(TAG, "", e);
				return false;
			} 
		}
	}
}
