/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
