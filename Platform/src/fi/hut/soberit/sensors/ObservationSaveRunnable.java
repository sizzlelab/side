package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.Vector;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.util.Log;
import eu.mobileguild.utils.ThreadUtil;


public class ObservationSaveRunnable implements Runnable {

	private static final String TAG = ObservationSaveRunnable.class.getSimpleName();
	
	private static final int DEFAULT_DATABASE_LOCK_WAIT_TIME = 1000;

	private static final int DEFAULT_SENSORS_BUFFER_SIZE = 10;

	private int sensorsBufferSize = DEFAULT_SENSORS_BUFFER_SIZE;
	
	private int databaseLockWaitTime = DEFAULT_DATABASE_LOCK_WAIT_TIME;
	
	private Vector<ContentValues> queue;
	
	private InsertHelper insertHelper;
	
	DatabaseHelper dbHelper;
	
	public ObservationSaveRunnable(Vector<ContentValues> queue, DatabaseHelper dbHelper, String table) {
		this.queue = queue;
		this.dbHelper = dbHelper;
		
		insertHelper = new DatabaseUtils.InsertHelper(
				dbHelper.getWritableDatabase(), 
				table);
	}
	
	@Override
	public void run() {
		Log.d(TAG, "ObservationSaveRunnable::run");
		try {
			final ArrayList<ContentValues> copy = new ArrayList<ContentValues>();

			while(!Thread.currentThread().isInterrupted()) {
				while(queue.size() < sensorsBufferSize) {
					Thread.sleep(databaseLockWaitTime);
				}
				
				synchronized(queue) {
					int copySize = Math.min(10, queue.size());

					for(int i = copySize -1; i >= 0; i--) {
						final ContentValues observation = queue.remove(i);
												
						copy.add(0, observation);
					}
				}
				
				while(copy.size() > 0) {
					ThreadUtil.throwIfInterruped();
									
					if (insertObservationFast(copy.get(0)) != -1) {
						copy.remove(0);
						continue;
					}

					Thread.sleep(50);
				}
			}
		} catch (IllegalStateException ise) {

			ise.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
			
	}
	
	protected long insertObservationFast(ContentValues values) {
		
		if (dbHelper.getWritableDatabase().isDbLockedByOtherThreads()) {
			Log.v(TAG, "Database was locked by another thread");
			return -1;
		}
		
		return insertHelper.insert(values);
	}

	public int getSensorsBufferSize() {
		return sensorsBufferSize;
	}

	public void setSensorsBufferSize(int sensorsBufferSize) {
		this.sensorsBufferSize = sensorsBufferSize;
	}

	public int getDatabaseLockWaitTime() {
		return databaseLockWaitTime;
	}

	public void setDatabaseLockWaitTime(int databaseLockWaitTime) {
		this.databaseLockWaitTime = databaseLockWaitTime;
	}
}