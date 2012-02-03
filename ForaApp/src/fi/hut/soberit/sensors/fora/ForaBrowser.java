package fi.hut.soberit.sensors.fora;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;

import eu.mobileguild.db.MGDatabaseHelper;
import eu.mobileguild.utils.LittleEndian;
import fi.hut.soberit.fora.D40Sink;
import fi.hut.soberit.fora.IR21Sink;
import fi.hut.soberit.sensors.BindOnDriverStartStrategy;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.MessagesListener;
import fi.hut.soberit.sensors.ObservationsListener;
import fi.hut.soberit.sensors.SinkDriverConnection;
import fi.hut.soberit.sensors.SinkService;
import fi.hut.soberit.sensors.activities.SessionHelper;
import fi.hut.soberit.sensors.fora.db.Ambient;
import fi.hut.soberit.sensors.fora.db.AmbientDao;
import fi.hut.soberit.sensors.fora.db.BloodPressure;
import fi.hut.soberit.sensors.fora.db.BloodPressureDao;
import fi.hut.soberit.sensors.fora.db.DatabaseHelper;
import fi.hut.soberit.sensors.fora.db.Glucose;
import fi.hut.soberit.sensors.fora.db.GlucoseDao;
import fi.hut.soberit.sensors.fora.db.Pulse;
import fi.hut.soberit.sensors.fora.db.PulseDao;
import fi.hut.soberit.sensors.fora.db.Temperature;
import fi.hut.soberit.sensors.fora.db.TemperatureDao;
import fi.hut.soberit.sensors.generic.GenericObservation;


public class ForaBrowser extends FragmentActivity 
	implements SensorStatusController, SensorStatusListener, MessagesListener, ObservationsListener {
    
	private static final String IR21_SESSION_ID_PREFERENCE = "ir21 session";
	private static final String D40_SESSION_ID_PREFERENCE = "d40 session";
	private static final String TAG = ForaBrowser.class.getSimpleName();

	public static final int CMD_DOWNLOAD = 1;

	
	ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;

	ExecutorService pool;
	
	DatabaseHelper dbHelper;
	
	private HashMap<Long, SensorStatusListener> typeListenerMap = new HashMap<Long, SensorStatusListener>();
	private SessionHelper d40Session;
	private SessionHelper ir21Session;

	private HashMap<String, ArrayList<Long>> driverTypes = new HashMap<String, ArrayList<Long>>();
	private ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();
	
	private HashMap<String, Integer> sensorStates = new HashMap<String, Integer>();
	
//	private HashMap<String, Integer> sensorCommand = new HashMap<String, Integer>();
	
	private BindOnDriverStartStrategy pingBackReceiver;
	
	private String clientId = getClass().getName();
	private TabPageIndicator mIndicator;
	private boolean backPressed;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);

        buildDriverAndUploadersTree(savedInstanceState);
        
        setContentView(R.layout.actionbar_tabs_pager);
//        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
       
        ActionBar.Tab tab1 = getSupportActionBar().newTab().setText("Pulse");
        ActionBar.Tab tab2 = getSupportActionBar().newTab().setText("Blood Pressure");
        ActionBar.Tab tab3 = getSupportActionBar().newTab().setText("Glucose");
        ActionBar.Tab tab4 = getSupportActionBar().newTab().setText("Temperature");
        ActionBar.Tab tab5 = getSupportActionBar().newTab().setText("Ambient");
                
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, mViewPager);

    	mTabsAdapter.addTab(tab1, SimpleObservationListFragment.class, bundleFactory(DriverInterface.TYPE_INDEX_PULSE));
    	mTabsAdapter.addTab(tab2, SimpleObservationListFragment.class, bundleFactory(DriverInterface.TYPE_INDEX_BLOOD_PRESSURE));
    	mTabsAdapter.addTab(tab3, SimpleObservationListFragment.class, bundleFactory(DriverInterface.TYPE_INDEX_GLUCOSE));
    	mTabsAdapter.addTab(tab4, SimpleObservationListFragment.class, bundleFactory(DriverInterface.TYPE_INDEX_TEMPERATURE));
    	mTabsAdapter.addTab(tab5, SimpleObservationListFragment.class, bundleFactory(DriverInterface.TYPE_INDEX_AMBIENT_TEMPERATURE));
    	
    	
		mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);
    	
        if (savedInstanceState != null) {
        	mTabsAdapter.setTabSelected(savedInstanceState.getInt("index"));
        }
        
        pool = Executors.newSingleThreadExecutor();
        
		dbHelper = new DatabaseHelper(this);
		
		d40Session = new SessionHelper(this, null);
		d40Session.setSessionIdPreference(D40_SESSION_ID_PREFERENCE);
		d40Session.setRegisterInDatabase(false);
		
		ir21Session = new SessionHelper(this, null);
		ir21Session.setSessionIdPreference(IR21_SESSION_ID_PREFERENCE);
		ir21Session.setRegisterInDatabase(false);		
		
		pingBackReceiver = new BindOnDriverStartStrategy(connections);
    }
    
    Bundle bundleFactory(long type) {
    	final Bundle bundle = new Bundle();
    	
    	bundle.putLong(SimpleObservationListFragment.TYPE_PARAM, type);
    	
    	return bundle;
    }
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "onResume");
    	super.onResume();
    	
    	if (d40Session.hasStarted()) {
    		final DriverConnection connection = addDriverConnection(D40Sink.ACTION, d40Session);
    		
    		connection.bind(this);
    	}
    	
    	if (ir21Session.hasStarted()) {
    		final DriverConnection connection = addDriverConnection(IR21Sink.ACTION, ir21Session);
    		
    		connection.bind(this);
    	}    	
    }


	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index", getSupportActionBar().getSelectedNavigationIndex());
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause");
		super.onPause();
		
		try {
			unregisterReceiver(pingBackReceiver);
		} catch(IllegalArgumentException e) {
			
		}
				
		for(DriverConnection connection: connections) {
			connection.unbind(this);
		}
    	
    	dbHelper.closeDatabases();
    	
    	if (backPressed) {
    		// a bit of a hack. We should stop all drivers from driverType
    		stopSession(DriverInterface.TYPE_INDEX_TEMPERATURE);
    		stopSession(DriverInterface.TYPE_INDEX_BLOOD_PRESSURE);
    	}
    }
    

	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
		ArrayList<Long> foraTypes = new ArrayList<Long>();		
		
		foraTypes.add(DriverInterface.TYPE_INDEX_BLOOD_PRESSURE);
		foraTypes.add(DriverInterface.TYPE_INDEX_GLUCOSE);
		foraTypes.add(DriverInterface.TYPE_INDEX_PULSE);
		
		driverTypes.put(D40Sink.ACTION, foraTypes);
		
		
		foraTypes = new ArrayList<Long>();		
		
		foraTypes.add(DriverInterface.TYPE_INDEX_TEMPERATURE);
		foraTypes.add(DriverInterface.TYPE_INDEX_AMBIENT_TEMPERATURE);
		
		driverTypes.put(IR21Sink.ACTION, foraTypes);
	}


	@Override
	public void onReceiveObservations(DriverConnection connection, final List<Parcelable> observations) {
		
		final SaveObservationsTask task = new SaveObservationsTask(connection, dbHelper);
		
		task.execute(observations);
	}

	class SaveObservationsTask extends AsyncTask<List<Parcelable>, Void, Void> {

		private DriverConnection connection;

		private BloodPressureDao pressureDao;
		private PulseDao pulseDao;
		private GlucoseDao glucoseDao;
		private TemperatureDao temperatureDao;
		private AmbientDao ambientDao;

		
		public SaveObservationsTask(DriverConnection connection, MGDatabaseHelper dbHelper) {
			this.connection = connection;
			
			pressureDao = new BloodPressureDao(dbHelper);
			pulseDao = new PulseDao(dbHelper);
			glucoseDao = new GlucoseDao(dbHelper);
			temperatureDao = new TemperatureDao(dbHelper);
			ambientDao = new AmbientDao(dbHelper);
		}
		
		@Override
		protected Void doInBackground(List<Parcelable>... params) {
			for(Parcelable parcelable: params[0]) {
				GenericObservation value = (GenericObservation) parcelable;
				
				long typeId = value.getObservationTypeId();
				if (typeId == DriverInterface.TYPE_INDEX_BLOOD_PRESSURE) {
					pressureDao.insert(new BloodPressure(
							value.getTime(),
							LittleEndian.readInt(value.getValue(), 0),
							LittleEndian.readInt(value.getValue(), 4)
							));
				} else if (typeId == DriverInterface.TYPE_INDEX_GLUCOSE) {
					glucoseDao.insert(new Glucose(
							value.getTime(),
							LittleEndian.readInt(value.getValue(), 0),
							LittleEndian.readInt(value.getValue(), 4)
							));
				} else if (typeId == DriverInterface.TYPE_INDEX_PULSE) {
					pulseDao.insert(new Pulse(
							value.getTime(),
							LittleEndian.readInt(value.getValue(), 0)
							));					
				} else if (typeId == DriverInterface.TYPE_INDEX_TEMPERATURE) {
					temperatureDao.insert(new Temperature(
							value.getTime(),
							LittleEndian.readFloat(value.getValue(), 0)
							));						
				} else if (typeId == DriverInterface.TYPE_INDEX_AMBIENT_TEMPERATURE) {
					ambientDao.insert(new Ambient(
							value.getTime(),
							LittleEndian.readFloat(value.getValue(), 0)
							));					
				}
				
			}
			return null;
		}
		
		public void onPostExecute(Void result) {
			onSensorStatusChanged(connection, SensorStatusListener.STAND_BY);
		}
	}
	
	@Override
	public void onSensorStatusChanged(DriverConnection connection, int newStatus) {
		Log.d(TAG, String.format("onSensorStatusChanged %s %d", connection.getDriverAction(), newStatus));
		
		sensorStates.put(connection.getDriverAction(), newStatus);
		
		final ArrayList<Long> types = driverTypes.get(connection.getDriverAction());
		
		if (types == null) {
			return;
		}
		
		for (Long type: types) {
			final SensorStatusListener listener = typeListenerMap.get(type);
			
			if (listener == null) {
				continue;
			}
			
			listener.onSensorStatusChanged(connection, newStatus);
		}		
		
		if (newStatus == SensorStatusListener.SENSOR_CONNECTED) {
			((SinkDriverConnection) connection).sendReadObservationNumberMessage();
		}
	}
	
	@Override
	public void onReceivedMessage(DriverConnection connection, Message msg) {
		Log.d(TAG, String.format("onReceivedMessage %s %d", connection.getDriverAction(), msg.what));
		
		int observationNum = msg.arg1;
		switch(msg.what) {
		case DriverInterface.MSG_SINK_OBJECTS_NUM:
			
			Log.d(TAG, "Sink object number is " + observationNum);
			
			final ArrayList<Long> types = driverTypes.get(connection.getDriverAction());
			
			((SinkDriverConnection) connection).sendReadObservations(types, 0, observationNum);
			
			onSensorStatusChanged(connection, SensorStatusListener.DOWNLOADING);
			
			break;
		}
	}

	public void stopSession(long typeId) {
		final DriverConnection connection = findDriverConnectionByTypeId(typeId);
		
		if (connection == null) {
			return;
		}
		
		connection.unbind(this);
		
		connections.remove(connection);
		
		final Intent stopSink = new Intent();
		stopSink.setAction(connection.getDriverAction());
		stopService(stopSink);
		
		onSensorStatusChanged(connection, SensorStatusListener.SENSOR_DISCONNECTED);
	}
	
	public void startSession(long typeId) {
		final SharedPreferences prefs = getSharedPreferences(ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		
		final String driver = findDriverByTypeId(typeId);
		DriverConnection connection = null;
		
		if (D40Sink.ACTION.equals(driver)) {

    		d40Session.startSession();
    		connection = addDriverConnection(driver, d40Session);
			
    		final String btAddress = prefs.getString(ForaSettings.D40_BLUETOOTH_ADDRESS, null);
    		
    		if (btAddress == null) {
    			Toast.makeText(this, R.string.no_d40_bluetooth_address, Toast.LENGTH_LONG).show();
    			return;
    		}
    		
			final Intent foraD40 = new Intent(this, D40Sink.class);
			foraD40.putExtra(SinkService.INTENT_DEVICE_ADDRESS, btAddress);
			startService(foraD40);
			
		} else {
			
    		ir21Session.startSession();
    		connection = addDriverConnection(driver, ir21Session);

    		final String btAddress = prefs.getString(ForaSettings.IR21_BLUETOOTH_ADDRESS, null); 
    		
    		if (btAddress == null) {
    			Toast.makeText(this, R.string.no_ir21_bluetooth_address, Toast.LENGTH_LONG).show();
    			return;
    		}
    		
			final Intent foraIR21 = new Intent(this, IR21Sink.class);
			foraIR21.putExtra(SinkService.INTENT_DEVICE_ADDRESS, btAddress);
			startService(foraIR21);
		}
				
		registerForPingBackOnStart();
	
		onSensorStatusChanged(connection, SensorStatusListener.CONNECTING);
	}

	private void registerForPingBackOnStart() {
		try {
			unregisterReceiver(pingBackReceiver);
		} catch (IllegalArgumentException e) {
			
		}
		
		final IntentFilter pingBackFilter = new IntentFilter();
		for (DriverConnection conn: connections) {
			final String action = conn.getDriverAction() + BroadcastingService.STARTED_PREFIX;
			
			pingBackFilter.addAction(action);
			Log.d(TAG, "Registered pingBackReceiver for " + action);
		}

		registerReceiver(pingBackReceiver, pingBackFilter);
	}

	private DriverConnection addDriverConnection(String driverAction, SessionHelper sessionHelper) {
		final SinkDriverConnection driverConnection = new SinkDriverConnection(driverAction, clientId);
		
		driverConnection.setMessagesListener(this);
		driverConnection.setObservationsListener(this);
		driverConnection.addSensorStatusListener(this);

		
		driverConnection.setSessionId(sessionHelper.getSessionId());
			
		connections.add(driverConnection);
		
		return driverConnection;
	}
	
	public String findDriverByTypeId(long typeId) {
		for (String driver: driverTypes.keySet()) {
			for (Long type: driverTypes.get(driver)) {
				if (type != typeId) {
					continue;
				}
				
				return driver;
			}
		}
		
		return null;
	}

	public DriverConnection findDriverConnectionByTypeId(long typeId) {
		for (DriverConnection connection: connections) {
			for (Long type: driverTypes.get(connection.getDriverAction())) {
				if (type != typeId) {
					continue;
				}
				
				return connection;
			}
		}
		
		return null;
	}
	
	public void registerConnectivityStatusListener(long id, SensorStatusListener listener) {
		typeListenerMap.put((Long) id, listener);
	}
	
	public void unregisterConnectivityStatusListener(long id) {
		typeListenerMap.remove(id);
	}
	
	@Override
	public int getSensorStatus(long typeId) {
		
		final String driverAction = findDriverByTypeId(typeId);

		if (!sensorStates.containsKey(driverAction)) {
			Log.d(TAG, "typeId " + typeId + " status: " + SensorStatusListener.SENSOR_DISCONNECTED);
			return SensorStatusListener.SENSOR_DISCONNECTED;
			
		}
		int newStates =  sensorStates.get(driverAction);
		
		Log.d(TAG, "typeId " + typeId + " status: " + newStates);
		
		return newStates;
	}

	public void refreshData(long typeId) {
		
		final SinkDriverConnection connection = (SinkDriverConnection) findDriverConnectionByTypeId(typeId);
				
		connection.sendReadObservationNumberMessage();		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		backPressed = true;
	}
}