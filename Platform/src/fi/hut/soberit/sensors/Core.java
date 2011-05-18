package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import eu.mobileguild.utils.ThreadUtil;
import fi.hut.soberit.sensors.generic.GenericObservation;

public class Core extends Service {

	public static final String TAG = Core.class.getSimpleName();
	
	public static final String INTENT_SESSION_ID = "sessionId";

	private static final String SHARED_PREFS = null;

	private static final String SESSION_IN_PROCESS = "session in process";
	
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	
	private Vector<ContentValues> observationsQueue = new Vector<ContentValues>();

	private Thread databaseThread;

	final ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();

	private DatabaseHelper dbHelper;
	private DriverDao driverDao;

	private long sessionId = 0;

	private ObservationTypeDao observationTypeDao;

	private List<DriverInfo> drivers;

	private ObservationKeynameDao observationKeynameDao;
	
	public Core() {
		dbHelper = new DatabaseHelper(this);
		
		driverDao = new DriverDao(dbHelper);
		
		observationTypeDao = new ObservationTypeDao(dbHelper);
		
		observationKeynameDao = new ObservationKeynameDao(dbHelper);
	}
	
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return START_REDELIVER_INTENT;
		}
		
		sessionId = intent.getLongExtra(Core.INTENT_SESSION_ID, -1);
		
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		
		final Editor editor = prefs.edit();
		editor.putLong(SESSION_IN_PROCESS, sessionId);
		editor.commit();
		
		drivers = driverDao.getEnabledDriverList();
		
		for(DriverInfo driver: drivers) {
			driver.setObservationTypes(
					observationTypeDao.getObservationTypeShort(driver.getId()));
			
			for(ObservationTypeShort type: driver.getObservationTypes()) {
				observationKeynameDao.getKeynamesShort(type.getId());
			}
			
			final DriverConnection driverConnection = new DriverConnection(driver);
			connections.add(driverConnection);	

			Log.d(TAG, "doBindService");
			
			final Intent driverIntent = new Intent();
			driverIntent.setAction(driver.getUrl());
			
			bindService(intent, driverConnection, Context.BIND_AUTO_CREATE);
		}
		
		return START_REDELIVER_INTENT;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");

		
		for(DriverConnection connection : connections) {
			connection.unregisterClient();

			// Detach our existing connection.
			unbindService(connection);
		}

	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}	
	
	class DriverConnection extends Handler implements ServiceConnection  {

		private Messenger serviceMessenger = null;
		
		private Messenger feedbackMessager = new Messenger(this);
		
		final DriverInfo driver;
		
		public DriverConnection(DriverInfo driver) {
			this.driver = driver;
		}
		
		public void unregisterClient() {
			try {
				Message msg = Message.obtain(null,
						DriverInterface.MSG_UNREGISTER_CLIENT);
				msg.replyTo = feedbackMessager;
				serviceMessenger.send(msg);
			} catch (RemoteException e) {
				Log.d(TAG, "No worries, service has crashed. No need to do anything: ", e);
			}			
		}

		public void onServiceConnected(ComponentName className, IBinder service) {
			serviceMessenger = new Messenger(service);
			Log.d(TAG, "Attached.");

			registerClient();

			Toast.makeText(Core.this, 
					R.string.remote_service_connected,
					Toast.LENGTH_SHORT).show();
		}

		private void registerClient() {
			try {

				Message msg = Message.obtain(null,
						DriverInterface.MSG_REGISTER_CLIENT);
				
				final int size = driver.getObservationTypes().size();
				final String[] mimeTypes = new String[size];
				final long[] ids = new long[size];
				
				final ArrayList<ObservationTypeShort> types =(ArrayList<ObservationTypeShort>) driver.getObservationTypes();
				
				for(int i = 0; i<size; i++) {
					final ObservationTypeShort type = types.get(i);
				
					mimeTypes[i] = type.getMimeType();
					ids[i] = type.getId();
				}
				
				final Bundle conf = new Bundle();
				conf.putStringArray(
						DriverInterface.MSG_FIELD_DATA_TYPES, 
						mimeTypes);
				
				conf.putLongArray(
						DriverInterface.MSG_FIELD_DATE_TYPE_IDS, 
						ids);
				
				msg.setData(conf);
				
				msg.replyTo = feedbackMessager;
				serviceMessenger.send(msg);
			} catch (RemoteException e) {

			}
		}

		@Override
		public void handleMessage(Message msg) {
						
			switch (msg.what) {
			case DriverInterface.MSG_OBSERVATION:
				
				final Bundle bundle = msg.getData();
				bundle.setClassLoader(Core.class.getClassLoader());

				final Parcelable[] observations = (Parcelable[]) bundle.getParcelableArray(DriverInterface.MSG_FIELD_OBSERVATIONS);
				Log.d(TAG, "Received " + observations.length);
				
				final ObservationTypeShort type = driver.getObservationType(msg.arg1); 
				
				for(Parcelable observation : observations) {
					addToQueue(type, (GenericObservation)observation);
				}
				
				break;
			default:
				super.handleMessage(msg);
			}
		}

		
		public void onServiceDisconnected(ComponentName className) {

			serviceMessenger = null;
			Log.d(TAG, "Disconnected.");

			// As part of the sample, tell the user what happened.
			Toast.makeText(Core.this, 
					R.string.remote_service_disconnected,
					Toast.LENGTH_SHORT).show();
		}

		void setServiceMessenger(Messenger serviceMessenger) {
			this.serviceMessenger = serviceMessenger;
		}

		Messenger getServiceMessenger() {
			return serviceMessenger;
		}
	}

	public void addToQueue(ObservationTypeShort type, GenericObservation observation) {
		long [] keynameIds = type.getKeynames();
		
		for(int i=0; i<type.getKeynamesNum(); i++) {
			final ContentValues values = new ContentValues();
			
			values.put("observation_type_id", type.getId());
			values.put("time", observation.getTime());

			values.put("observation_keyname_id", keynameIds[i]);
			values.put("value", observation.getValue(i));
			
			observationsQueue.add(values);		
		}
	};
	
	class DatabaseRunnable implements Runnable {

		private Vector<ContentValues> queue;
		private InsertHelper insertHelper;
		
		public DatabaseRunnable(Vector<ContentValues> queue) {
			this.queue = queue;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "DatabaseInsertQueueThread::run");
			try {
				final ArrayList<ContentValues> copy = new ArrayList<ContentValues>();

				while(!Thread.currentThread().isInterrupted()) {
					while(queue.size() < Configuration.SENSORS_BUFFER_SIZE) {
						Thread.sleep(1000);
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
		
		public long insertObservationFast(ContentValues values) {
			if (insertHelper == null) {
				insertHelper = new DatabaseUtils.InsertHelper(
					dbHelper.getWritableDatabase(), 
					DatabaseHelper.OBSERVATION_VALUE_TABLE);
			}
		
			Log.v(TAG, String.format("Insert observation session_id=%d, type=%d, %d", 
					sessionId, 
					values.get("observation_type_id"),
					values.get("time")));
			
			if (dbHelper.getWritableDatabase().isDbLockedByOtherThreads()) {
				Log.v(TAG, "Database was locked by another thread");
				return -1;
			}
			
			return insertHelper.insert(values);
		}
	}
}
