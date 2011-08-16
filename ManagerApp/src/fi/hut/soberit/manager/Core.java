package fi.hut.soberit.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import fi.hut.soberit.manager.snapshot.ManagerSettings;
import fi.hut.soberit.sensors.ConnectionKeepAliveWorker;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationSaveRunnable;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.UploadedTypeDao;
import fi.hut.soberit.sensors.UploaderDao;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.UploadedType;
import fi.hut.soberit.sensors.generic.Uploader;

// TODO Remove core and start using broadcast receiver for the purpose
public class Core extends Service  {

	public static final String TAG = Core.class.getSimpleName();
	
	public static final String INTENT_SESSION_ID = "sessionId";
	
	private ConnectionKeepAliveWorker connectionsKeptAlive;

	private long sessionId = 0;
	
	private List<Driver> drivers;

	private DriverDao driverDao;
	
	private ObservationTypeDao observationTypeDao;

	private ObservationKeynameDao observationKeynameDao;

	private DatabaseHelper sessionsDbHelper;
		
	final Map<Long, GenericObservation> snapshot 
		= new HashMap<Long, GenericObservation>();

	private UploaderDao uploaderDao;

	private UploadedTypeDao uploadedTypeDao;

	private SessionDao sessionDao;
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent == null) {
			return START_REDELIVER_INTENT;
		}
		
		sessionId = intent.getLongExtra(Core.INTENT_SESSION_ID, -1);
		
		final SharedPreferences prefs = getSharedPreferences(
				ManagerSettings.APP_PREFERENCES_FILE, 
				MODE_PRIVATE);
		
		final Editor editor = prefs.edit();
		editor.putLong(ManagerSettings.SESSION_IN_PROCESS, sessionId);
		editor.commit();
		
		sessionsDbHelper = new DatabaseHelper(this);
		sessionDao = new SessionDao(sessionsDbHelper);
				
		driverDao = new DriverDao(sessionsDbHelper);
		observationTypeDao = new ObservationTypeDao(sessionsDbHelper);		
		observationKeynameDao = new ObservationKeynameDao(sessionsDbHelper);

		drivers = driverDao.getEnabledDriverList();
		
		ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();
		
		ArrayList<ObservationType> allTypes = new ArrayList<ObservationType>();

		for(Driver driver: drivers) {
			List<ObservationType> types = observationTypeDao.getObservationType(driver.getId(), true);
			
			for(ObservationType type: types) {
				type.setKeynames(observationKeynameDao.getKeynames(type.getId()));
				type.setDriver(driver);
			}			
			
			final CoreConnection driverConnection = new CoreConnection(driver, types);
			driverConnection.bind(this);
			driverConnection.setSessionId(sessionId);
			
			connections.add(driverConnection);
			
			allTypes.addAll(types);
		}
		
		uploaderDao = new UploaderDao(sessionsDbHelper);
		uploadedTypeDao = new UploadedTypeDao(sessionsDbHelper);
		
		final ArrayList<Uploader> uploaders = uploaderDao.getUploaders(Boolean.TRUE);
		
		for(Uploader uploader: uploaders) {
			final List<UploadedType> list = uploadedTypeDao.getTypes(uploader.getId()); 
			
			final UploadedType[] uploadedTypes = new UploadedType[list.size()];
			uploader.setUploadedTypes(list.toArray(uploadedTypes));
		}
		
		connectionsKeptAlive = new ConnectionKeepAliveWorker(connections, this); 
		
		final Intent sessionStartedBroadcast = new Intent();
		sessionStartedBroadcast.setAction(DriverInterface.ACTION_SESSION_STARTED);
		sessionStartedBroadcast.putExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, allTypes);
		sessionStartedBroadcast.putExtra(DriverInterface.INTENT_FIELD_UPLOADERS, uploaders);
		
		sendBroadcast(sessionStartedBroadcast);
		
		return START_REDELIVER_INTENT;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");

		final Intent sessionStartedBroadcast = new Intent();
		sessionStartedBroadcast.setAction(DriverInterface.ACTION_SESSION_STOP);
		
		sendBroadcast(sessionStartedBroadcast);
		
		if (connectionsKeptAlive != null) {
			connectionsKeptAlive.stop();
			
			for(DriverConnection connection : connectionsKeptAlive.getConnections()) {
				if (!connection.isServiceConnected()) {
					continue;
				}
				
				connection.unregisterClient();
				unbindService(connection);
			}
		}

		final SharedPreferences prefs = getSharedPreferences(
				ManagerSettings.APP_PREFERENCES_FILE, 
				MODE_PRIVATE);
		
		final Editor editor = prefs.edit();
		editor.remove(ManagerSettings.SESSION_IN_PROCESS);
		editor.commit();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		
		return new CoreImpl();
	}	
	
	class CoreConnection extends DriverConnection {
		
		long lastSessionUpdate = 0;
		
		public CoreConnection(Driver driver, List<ObservationType> types) {
			super(driver, types, true);
		}

		public void onReceiveObservations(List<Parcelable> observations) {
			if (observations.size() > 0) {
				final GenericObservation lastest = (GenericObservation)observations.get(0);
				snapshot.put(lastest.getObservationTypeId(), lastest);
			}
			
			final long now = System.currentTimeMillis();
			
			if (now - lastSessionUpdate < 60*1000) {
				return;
			}
			
			lastSessionUpdate = now;
			
			sessionDao.updateSession(sessionId, now);
		}
		
		public void onServiceConnected(ComponentName className, IBinder service) {
			super.onServiceConnected(className, service);
			
			Toast.makeText(Core.this, 
					R.string.remote_service_connected,
					Toast.LENGTH_SHORT).show();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			super.onServiceDisconnected(className);
			
			Toast.makeText(Core.this, 
					R.string.remote_service_disconnected,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	class CoreImpl extends ICore.Stub {

		@Override
		public Map getSnapshot() throws RemoteException {
			return Core.this.snapshot;
		}
	}
	
}
