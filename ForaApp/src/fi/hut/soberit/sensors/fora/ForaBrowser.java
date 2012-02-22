package fi.hut.soberit.sensors.fora;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

import eu.mobileguild.bluetooth.BluetoothPairingOneDeviceCheck;
import eu.mobileguild.db.MGDatabaseHelper;
import eu.mobileguild.utils.IntentFilterFactory;
import eu.mobileguild.utils.LittleEndian;
import fi.hut.soberit.fora.D40Sink;
import fi.hut.soberit.fora.IR21Sink;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.DriverStarter;
import fi.hut.soberit.sensors.DriverStatusListener;
import fi.hut.soberit.sensors.MessagesListener;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.SensorSinkService;
import fi.hut.soberit.sensors.SensorSinkActivityListener;
import fi.hut.soberit.sensors.SinkDriverConnection;
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
	implements SensorStatusController, DriverStatusListener, MessagesListener {

	private static final String TAG = ForaBrowser.class.getSimpleName();

	private static final String TAB_INDEX = "index";
	private static final String IR21_SESSION_ID_PREFERENCE = "ir21 session";
	private static final String D40_SESSION_ID_PREFERENCE = "d40 session";

	private static final String D40_BT_ADDRESS = "d40 address";
	private static final String IR21_BT_ADDRESS = "ir21 address";

	private static final int REQUEST_CHOOSE_D40_DEVICE = 1;

	private static final int REQUEST_CHOOSE_IR21_DEVICE = 2;
	
	ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;

	ExecutorService pool;
	
	DatabaseHelper dbHelper;
	
	private HashMap<String, SensorSinkActivityListener> typeListenerMap = new HashMap<String, SensorSinkActivityListener>();
	private SessionHelper d40Session;
	private SessionHelper ir21Session;

	private ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();
	
	private HashMap<String, Integer> sensorStates = new HashMap<String, Integer>();
		
	private String clientId = getClass().getName();
	private TabPageIndicator mIndicator;
	private boolean backPressed;
	
	private static final long [] d40Types = new long [] {		
			DriverInterface.TYPE_INDEX_BLOOD_PRESSURE,
			DriverInterface.TYPE_INDEX_GLUCOSE,
			DriverInterface.TYPE_INDEX_PULSE
		};

	private static final long [] ir21Types = new long [] {
		DriverInterface.TYPE_INDEX_TEMPERATURE,
		DriverInterface.TYPE_INDEX_AMBIENT_TEMPERATURE
	};
	
	private String d40Address;
	private String ir21Address;


	private DriverStarter d40Starter;


	private DriverStarter ir21Starter;
	
    @Override
	protected void onCreate(Bundle sis) {
    	
        super.onCreate(sis);

        setContentView(R.layout.actionbar_tabs_pager);
       
        ActionBar.Tab tab1 = getSupportActionBar().newTab().setText(R.string.bpm_bgm_tab);
        ActionBar.Tab tab2 = getSupportActionBar().newTab().setText(R.string.thermometer_tab);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, mViewPager);

    	mTabsAdapter.addTab(tab1, SimpleObservationListFragment.class, bundleFactory(D40Sink.ACTION, d40Types));
    	mTabsAdapter.addTab(tab2, SimpleObservationListFragment.class, bundleFactory(IR21Sink.ACTION, ir21Types));
    	
		mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);
    	
        if (sis != null) {
        	mTabsAdapter.setTabSelected(sis.getInt(TAB_INDEX));
        	
        	d40Address = sis.getString(D40_BT_ADDRESS);
        	ir21Address = sis.getString(IR21_BT_ADDRESS);
        }
        
        pool = Executors.newSingleThreadExecutor();
        
		dbHelper = new DatabaseHelper(this);
		
		d40Session = new SessionHelper(this, null);
		d40Session.setSessionIdPreference(D40_SESSION_ID_PREFERENCE);
		d40Session.setRegisterInDatabase(false);
		
		ir21Session = new SessionHelper(this, null);
		ir21Session.setSessionIdPreference(IR21_SESSION_ID_PREFERENCE);
		ir21Session.setRegisterInDatabase(false);		
    }
    
	Bundle bundleFactory(String driverAction, long [] driverTypes) {
    	final Bundle bundle = new Bundle();
    	
    	bundle.putString(SimpleObservationListFragment.DRIVER_ACTION_PARAM, driverAction);
    	bundle.putLongArray(SimpleObservationListFragment.TYPES_PARAM, driverTypes);
    	
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
        outState.putInt(TAB_INDEX, getSupportActionBar().getSelectedNavigationIndex());
        
        outState.putString(D40_BT_ADDRESS, d40Address);
        outState.putString(IR21_BT_ADDRESS, ir21Address);
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause");
		super.onPause();
		
		try {
			if (d40Starter != null) {
				unregisterReceiver(d40Starter);
			}
		} catch(IllegalArgumentException e) {
			
		}

		try {
			if (ir21Starter != null) {
				unregisterReceiver(ir21Starter);
			}
		} catch (IllegalArgumentException e) {
			
		}
			
				
		for(DriverConnection connection: connections) {
			connection.unbind(this);
		}
		
		connections.clear();
    	
    	dbHelper.closeDatabases();
    	
    	if (backPressed) {
    		stopSession(D40Sink.ACTION);
    		stopSession(IR21Sink.ACTION);
    		
    		d40Session.stopSession();
    		ir21Session.stopSession();
    	}
    }

    @Override
	protected void onActivityResult (int requestCode, int resultCode, Intent result) {
    	
    	if (requestCode == REQUEST_CHOOSE_D40_DEVICE && resultCode == Activity.RESULT_OK) {
    		d40Address = result.getStringExtra(BluetoothPairingOneDeviceCheck.DEVICE_ADDRESS);
    		
    		final SharedPreferences prefs = getSharedPreferences(ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
    		final Editor editor = prefs.edit(); 
    		editor.putString(ForaSettings.D40_BLUETOOTH_ADDRESS, d40Address);
    		editor.commit();
    		
    		startSession(D40Sink.ACTION);
    	} else		
    	if (requestCode == REQUEST_CHOOSE_IR21_DEVICE && resultCode == Activity.RESULT_OK) {
    		ir21Address = result.getStringExtra(BluetoothPairingOneDeviceCheck.DEVICE_ADDRESS);
    		
    		final SharedPreferences prefs = getSharedPreferences(ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
    		final Editor editor = prefs.edit(); 
    		editor.putString(ForaSettings.IR21_BLUETOOTH_ADDRESS, ir21Address);
    		editor.commit();
    		
    		startSession(IR21Sink.ACTION);    		
    	} else
    	if (requestCode == REQUEST_CHOOSE_IR21_DEVICE && resultCode == Activity.RESULT_CANCELED) { 
			Toast.makeText(this, R.string.no_ir21_bluetooth_address, Toast.LENGTH_LONG).show();
    	} else 
		if (requestCode == REQUEST_CHOOSE_D40_DEVICE && resultCode == Activity.RESULT_CANCELED) {
			Toast.makeText(this, R.string.no_d40_bluetooth_address, Toast.LENGTH_LONG).show();
    	}
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
			setSensorSinkActivityStatus(connection, SensorSinkActivityListener.CONNECTED);
		}
	}
	
	@Override
	public void onDriverStatusChanged(DriverConnection connection, int newStatus) {
		final String driverAction = connection.getDriverAction();
		Log.d(TAG, String.format("onDriverStatusChanged %s %d", driverAction, newStatus));
		
		final int oldStatus = getSensorStatus(driverAction);
		newStatus = driverStatusToActivityStatus(oldStatus, newStatus);

		if (oldStatus == SensorSinkActivityListener.DOWNLOADING && 
			newStatus == SensorSinkActivityListener.CONNECTED) {

			// skipping this for now. Will setSensorSinkActionStatus after we refresh the database
			return;
		}
		
		setSensorSinkActivityStatus(connection, newStatus);
	}
	
	public void setSensorSinkActivityStatus(DriverConnection connection, int newStatus) {		
		final String driverAction = connection.getDriverAction();
		Log.d(TAG, String.format("setSensorSinkActivityStatus %s %d", driverAction, newStatus));


		final int oldStatus = getSensorStatus(driverAction);

		Log.d(TAG, String.format("old status: %d, new status: %d", oldStatus, newStatus));

		if (oldStatus == newStatus) {
			return;
		}
		
		
		if (oldStatus != SensorSinkActivityListener.DOWNLOADING && 
			newStatus == SensorSinkActivityListener.CONNECTED) {
			
			// important! new status have to be set ASAP
			newStatus = SensorSinkActivityListener.DOWNLOADING;
			((SinkDriverConnection) connection).sendReadObservationNumberMessage();
		} 			

		sensorStates.put(driverAction, newStatus);
		
		final SensorSinkActivityListener listener = typeListenerMap.get(driverAction);			
		if (listener != null) {
			listener.onSensorSinkStatusChanged(connection, newStatus);
		}
	}

	
	/**
	 *	Use this function for the cases when DriverConnection is yet to be created.  
	 * @param driverAction
	 * @param newStatus
	 */
	public void setSensorSinkActivityStatus(String driverAction, int newStatus) {		
		Log.d(TAG, String.format("setSensorSinkActivityStatus %s %d", driverAction, newStatus));


		final int oldStatus = getSensorStatus(driverAction);

		Log.d(TAG, String.format("old status: %d, new status: %d", oldStatus, newStatus));

		if (oldStatus == newStatus) {
			return;
		}
		
		sensorStates.put(driverAction, newStatus);
		
		final SensorSinkActivityListener listener = typeListenerMap.get(driverAction);			
		if (listener != null) {
			listener.onSensorSinkStatusChanged(null, newStatus);
		}
	}

	
	@Override
	public void onReceivedMessage(DriverConnection connection, Message msg) {
		Log.d(TAG, String.format("onReceivedMessage %s %d", connection.getDriverAction(), msg.what));
		
		int observationNum = msg.arg1;
		switch(msg.what) {
		case SensorSinkService.RESPONSE_COUNT_OBSERVATIONS:
			
			Log.d(TAG, "Sink object number is " + observationNum);
						
			long [] types = D40Sink.ACTION.equals(connection.getDriverAction())
					? d40Types
					: ir21Types;
			
			((SinkDriverConnection) connection).sendReadObservations(types, 0, observationNum);			
			break;
			
		case SensorSinkService.RESPONSE_READ_OBSERVATIONS:
			final Bundle bundle = msg.getData();
			bundle.setClassLoader(this.getClass().getClassLoader());

			Log.d(TAG, String.format("Received observations"));				 

			final List<Parcelable> observations = (List<Parcelable>) bundle.getParcelableArrayList(SensorSinkService.RESPONSE_FIELD_OBSERVATIONS);
			Log.d(TAG, String.format("Received observations from " + connection.getDriverAction()));				 

			
			final SaveObservationsTask task = new SaveObservationsTask(connection, dbHelper);
			
			task.execute(observations);
			break;
		}
	}

	@Override
	public void stopSession(String driverAction) {
		final DriverConnection connection = findDriverConnectionByDriverAction(driverAction);
		
		if (connection == null) {
			return;
		}
		
		connection.unbind(this);
		
		connections.remove(connection);
		
		final Intent stopSink = new Intent();
		stopSink.setAction(connection.getDriverAction());
		stopService(stopSink);
		
		onDriverStatusChanged(connection, DriverStatusListener.UNBOUND);
	}
	
	@Override
	public void startSession(String driverAction) {
		Log.d(TAG, "startSession");

		setSensorSinkActivityStatus(driverAction, SensorSinkActivityListener.CONNECTING);
		
		final SharedPreferences prefs = getSharedPreferences(ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);

		DriverConnection connection = null;
		
		if (D40Sink.ACTION.equals(driverAction)) {

			if (d40Address == null) {				
				chooseBtDevice(REQUEST_CHOOSE_D40_DEVICE, prefs.getString(ForaSettings.D40_BLUETOOTH_ADDRESS, null), false);
				return;
			}
			
    		d40Session.startSession();
    		connection = addDriverConnection(driverAction, d40Session);
			    					
			d40Starter = new DriverStarter((SinkDriverConnection) connection, d40Address);
			
			final IntentFilter filter = IntentFilterFactory.simpleActionFilter(BroadcastingService.encodePingBackAction(D40Sink.ACTION));
			registerReceiver(d40Starter, filter);
			
			final Intent foraD40 = new Intent(this, D40Sink.class);
			startService(foraD40);
			
		} else {

			if (ir21Address == null) {				
				chooseBtDevice(REQUEST_CHOOSE_IR21_DEVICE, prefs.getString(ForaSettings.IR21_BLUETOOTH_ADDRESS, null), false);
				return;
			}

			
    		ir21Session.startSession();
    		connection = addDriverConnection(driverAction, ir21Session);
			
			ir21Starter = new DriverStarter((SinkDriverConnection) connection, ir21Address);
		
			final IntentFilter filter = IntentFilterFactory.simpleActionFilter(BroadcastingService.encodePingBackAction(IR21Sink.ACTION));
			registerReceiver(ir21Starter, filter);

			final Intent foraIR21 = new Intent(this, IR21Sink.class);
			startService(foraIR21);
		}
		
	}

	private DriverConnection addDriverConnection(String driverAction, SessionHelper sessionHelper) {
		
		final SinkDriverConnection driverConnection = new SinkDriverConnection(driverAction, clientId);
		
		driverConnection.setMessagesListener(this);
		driverConnection.addDriverStatusListener(this);
		
		driverConnection.setSessionId(sessionHelper.getSessionId());
			
		connections.add(driverConnection);
		
		return driverConnection;
	}
	
	public DriverConnection findDriverConnectionByDriverAction(String driverAction) {
		for (DriverConnection connection: connections) {
			if (driverAction.equals(connection.getDriverAction())) {
				return connection;
			}
		}
		
		return null;
	}
	
	public void registerActivityStatusListener(String driverAction, SensorSinkActivityListener listener) {
		typeListenerMap.put(driverAction, listener);
	}
	
	public void unregisterConnectivityStatusListener(String driverAction) {
		typeListenerMap.remove(driverAction);
	}
	
	@Override
	public int getSensorStatus(String driverAction) {
		
		if (!sensorStates.containsKey(driverAction)) {
			Log.d(TAG, "Driver " + driverAction + " status: " + SensorSinkActivityListener.DISCONNECTED);
			return SensorSinkActivityListener.DISCONNECTED;
			
		}
		int status =  sensorStates.get(driverAction);
		
		Log.d(TAG, "getSensorStatus (" + driverAction + ") = " + status);
		
		return status;
	}

	public void refreshData(String driverAction) {
		
		final SinkDriverConnection connection = (SinkDriverConnection) findDriverConnectionByDriverAction(driverAction);
		
		Log.d(TAG, "refreshData " + connections.size());
		
		setSensorSinkActivityStatus(connection, SensorSinkActivityListener.DOWNLOADING);
		connection.sendReadObservationNumberMessage();		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		backPressed = true;
	}

	@Override
	public boolean isSensorPaired(String driverAction) {
//		final SharedPreferences prefs = getSharedPreferences(ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
//		
//		if (driverAction.equals(IR21Sink.ACTION) && 
//			prefs.getString(ForaSettings.IR21_BLUETOOTH_ADDRESS, null) == null) {
//			
//			Toast.makeText(this, R.string.no_ir21_bluetooth_address, Toast.LENGTH_LONG).show();
//			return false;
//		}
//		
//		if (driverAction.equals(D40Sink.ACTION) &&
//			prefs.getString(ForaSettings.D40_BLUETOOTH_ADDRESS, null) == null) {
//			
//			Toast.makeText(this, R.string.no_d40_bluetooth_address, Toast.LENGTH_LONG).show();
//			return false;
//		}
		
		return true;
	}

	private void chooseBtDevice(int requestCode, String address, boolean skip1step) {
		
		final Intent settings = new Intent(this, BluetoothPairingOneDeviceCheck.class);
		settings.putExtra(BluetoothPairingOneDeviceCheck.DEVICE_ADDRESS, address);
		settings.putExtra(BluetoothPairingOneDeviceCheck.PROCEED_TO_SECOND_STEP, true);
		settings.putExtra(BluetoothPairingOneDeviceCheck.INTERESTING_DEVICE_NAME_PREFIX, "taidoc");
		startActivityForResult(settings, requestCode);
	}
	
	public int driverStatusToActivityStatus(int oldStatus, int driverStatus) {
		
		switch(driverStatus) {
		case DriverStatusListener.UNBOUND:
		case DriverStatusListener.BOUND:
			return oldStatus != SensorSinkActivityListener.CONNECTING 
				? SensorSinkActivityListener.DISCONNECTED
				: SensorSinkActivityListener.CONNECTING;
		case DriverStatusListener.CONNECTING:
			return SensorSinkActivityListener.CONNECTING;
		case DriverStatusListener.CONNECTED:
			return SensorSinkActivityListener.CONNECTED;
		case DriverStatusListener.COUNTING:
		case DriverStatusListener.DOWNLOADING:
			return SensorSinkActivityListener.DOWNLOADING;
		}
		
		throw new RuntimeException("Shouldn't happen");		
	}
}