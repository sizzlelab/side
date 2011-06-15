package fi.hut.soberit.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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
import fi.hut.soberit.sensors.Configuration;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.DriverInfo;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.ui.Settings;

public class Core extends Service  {

	public static final String TAG = Core.class.getSimpleName();
	
	public static final String INTENT_SESSION_ID = "sessionId";
	
	private Vector<ContentValues> observationsQueue = new Vector<ContentValues>();

	private Thread databaseThread;

	final ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();

	private DriverDao driverDao;

	private long sessionId = 0;

	private ObservationTypeDao observationTypeDao;

	private List<DriverInfo> drivers;

	private ObservationKeynameDao observationKeynameDao;

	private DatabaseHelper sessionsDbHelper;
		
	final Map<Long, GenericObservation> snapshot 
		= new HashMap<Long, GenericObservation>();
	
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent == null) {
			return START_REDELIVER_INTENT;
		}
		
		sessionId = intent.getLongExtra(Core.INTENT_SESSION_ID, -1);
		
		final SharedPreferences prefs = getSharedPreferences(
				Settings.APP_PREFERENCES_FILE, 
				MODE_PRIVATE);
		
		final Editor editor = prefs.edit();
		editor.putLong(Settings.SESSION_IN_PROCESS, sessionId);
		editor.commit();

		
		sessionsDbHelper = new DatabaseHelper(this);
		
		driverDao = new DriverDao(sessionsDbHelper);
		observationTypeDao = new ObservationTypeDao(sessionsDbHelper);		
		observationKeynameDao = new ObservationKeynameDao(sessionsDbHelper);

		databaseThread = new Thread(new DatabaseRunnable(observationsQueue, new DatabaseHelper(this, sessionId)));		
		databaseThread.start();
		
		drivers = driverDao.getEnabledDriverList();
		
		for(DriverInfo driver: drivers) {
			driver.setObservationTypes(
					observationTypeDao.getObservationType(driver.getId(), true));
			
			for(ObservationType type: driver.getObservationTypes()) {
				type.setKeynames(observationKeynameDao.getKeynames(type.getId()));
			}			
			
			final DriverConnection driverConnection = new DriverConnection(driver);
			connections.add(driverConnection);	
			
			final Intent driverIntent = new Intent();
			driverIntent.setAction(driver.getUrl());			
			
			Log.d(TAG, "binding to " + driver.getUrl());
			Log.d(TAG, "result: " + bindService(driverIntent, driverConnection, Context.BIND_AUTO_CREATE));
		}
		
		return START_REDELIVER_INTENT;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");

		for(DriverConnection connection : connections) {
			connection.unregisterClient();

			unbindService(connection);
		}
		
		if (databaseThread != null && databaseThread.isAlive()) {
			databaseThread.interrupt();
		}
		
		final SharedPreferences prefs = getSharedPreferences(
				Settings.APP_PREFERENCES_FILE, 
				MODE_PRIVATE);
		
		final Editor editor = prefs.edit();
		editor.remove(Settings.SESSION_IN_PROCESS);
		editor.commit();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		
		return new CoreImpl();
	}	
	
	class DriverConnection extends Handler implements ServiceConnection  {

		private Messenger serviceMessenger = null;
		
		private Messenger feedbackMessager = new Messenger(this);
		
		final DriverInfo driver;
		
		final HashMap<Long, ObservationType> typesMap = new HashMap<Long, ObservationType>();

		
		public DriverConnection(DriverInfo driver) {
			this.driver = driver;
			
			for(ObservationType typeShort : driver.getObservationTypes()) {
				typesMap.put(typeShort.getId(), typeShort);
			}
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
				
				final ArrayList<ObservationType> types =(ArrayList<ObservationType>) driver.getObservationTypes();
				
				for(int i = 0; i<size; i++) {
					final ObservationType type = types.get(i);
				
					mimeTypes[i] = type.getMimeType();
					ids[i] = type.getId();
				}
				
				final Bundle conf = new Bundle();
				conf.putStringArray(
						DriverInterface.MSG_FIELD_DATA_TYPES, 
						mimeTypes);
				
				conf.putLongArray(
						DriverInterface.MSG_FIELD_DATA_TYPE_IDS, 
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

				Log.d(TAG, String.format("Received observations"));				 

				final List<Parcelable> observations = (List<Parcelable>) bundle.getParcelableArrayList(DriverInterface.MSG_FIELD_OBSERVATIONS);
				Log.d(TAG, String.format("Received '%d' observations", driver.getId()));				 
			
				if (observations.size() > 0) {
					final GenericObservation lastest = (GenericObservation)observations.get(0);
					snapshot.put(lastest.getObservationTypeId(), lastest);
				}
				
				for(Parcelable observation : observations) {
					addToQueue(typesMap, (GenericObservation)observation);
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

	public void addToQueue(HashMap<Long, ObservationType> typesMap, GenericObservation observation) {
		final ObservationType type = typesMap.get(observation.getObservationTypeId());
		
		ObservationKeyname [] keynames = type.getKeynames();
		
		for(int i=0; i<keynames.length; i++) {
			final ContentValues values = new ContentValues();
			
			values.put("observation_type_id", type.getId());
			values.put("time", observation.getTime());

			values.put("observation_keyname_id", keynames[i].getId());
			values.put("value", observation.getValue(i));
			
			observationsQueue.add(values);		
		}
	};
	
	class DatabaseRunnable implements Runnable {

		private Vector<ContentValues> queue;
		private InsertHelper insertHelper;
		
		DatabaseHelper dbHelper;
		
		public DatabaseRunnable(Vector<ContentValues> queue, DatabaseHelper dbHelper) {
			this.queue = queue;
			this.dbHelper = dbHelper;
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

	class CoreImpl extends ICore.Stub {

		@Override
		public Map getSnapshot() throws RemoteException {
			return Core.this.snapshot;
		}
	}
	
}
