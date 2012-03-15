package fi.hut.soberit.sensors.fora;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;

import eu.mobileguild.bluetooth.BluetoothPairingOneDeviceCheck;
import eu.mobileguild.bluetooth.LeanBluetoothPairingInterestingDevices;
import eu.mobileguild.db.MGDatabaseHelper;
import eu.mobileguild.utils.LittleEndian;
import fi.hut.soberit.fora.D40Sink;
import fi.hut.soberit.fora.IR21Sink;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.DriverStatusListener;
import fi.hut.soberit.sensors.MessagesListener;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.SensorSinkActivityListener;
import fi.hut.soberit.sensors.SensorSinkService;
import fi.hut.soberit.sensors.SinkDriverConnection;
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

public class ForaBrowser extends FragmentActivity implements
		SensorStatusController, DriverStatusListener, MessagesListener {

	private static final String TAG = ForaBrowser.class.getSimpleName();

	private static final String TAB_INDEX = "index";

	private static final int REQUEST_CHOOSE_D40_DEVICE = 1;

	private static final int REQUEST_CHOOSE_IR21_DEVICE = 2;

	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;

	ExecutorService pool;

	DatabaseHelper dbHelper;

	private HashMap<String, SensorSinkActivityListener> typeListenerMap = new HashMap<String, SensorSinkActivityListener>();

	private ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();

	private HashMap<String, Integer> sensorStates = new HashMap<String, Integer>();

	private String clientId = getClass().getName();
	private TabPageIndicator mIndicator;

	private static final long[] d40Types = new long[] {
			DriverInterface.TYPE_INDEX_BLOOD_PRESSURE,
			DriverInterface.TYPE_INDEX_GLUCOSE,
			DriverInterface.TYPE_INDEX_PULSE };

	private static final long[] ir21Types = new long[] {
			DriverInterface.TYPE_INDEX_TEMPERATURE,
			DriverInterface.TYPE_INDEX_AMBIENT_TEMPERATURE };

	private static final int CONNECTION_TIMEOUT = 5000;

	public static String fORA_DEVICES_PREFIX = "taidoc";

	@Override
	protected void onCreate(Bundle sis) {

		super.onCreate(sis);

		setContentView(R.layout.actionbar_tabs_pager);

		final ActionBar.Tab tab1 = getSupportActionBar().newTab().setText(
				R.string.bpm_bgm_tab);
		final ActionBar.Tab tab2 = getSupportActionBar().newTab().setText(
				R.string.thermometer_tab);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(tab1, SimpleObservationListFragment.class,
				bundleFactory(D40Sink.ACTION, d40Types));
		mTabsAdapter.addTab(tab2, SimpleObservationListFragment.class,
				bundleFactory(IR21Sink.ACTION, ir21Types));

		mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);

		if (sis != null) {
			mTabsAdapter.setTabSelected(sis.getInt(TAB_INDEX));
		}

		pool = Executors.newSingleThreadExecutor();

		dbHelper = new DatabaseHelper(this);

//		handler = new Handler();
		
		final DriverConnection d40Connection = addDriverConnection(D40Sink.ACTION);
		d40Connection.bind(this);
		connections.add(d40Connection);
		
		final DriverConnection ir21Connection = addDriverConnection(IR21Sink.ACTION);
		ir21Connection.bind(this);
		connections.add(ir21Connection);
	}

	Bundle bundleFactory(String driverAction, long[] driverTypes) {
		final Bundle bundle = new Bundle();

		bundle.putString(SimpleObservationListFragment.DRIVER_ACTION_PARAM,
				driverAction);
		bundle.putLongArray(SimpleObservationListFragment.TYPES_PARAM,
				driverTypes);

		return bundle;
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TAB_INDEX, getSupportActionBar()
				.getSelectedNavigationIndex());
	}
	
	@Override
	public void onBackPressed() {
		for (DriverConnection connection : connections) {
			((SinkDriverConnection) connection).sendDisconnectRequest();
		}
				
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

//		if (connectionCheckTask != null) {
//			handler.removeCallbacks(connectionCheckTask);
//		}
	}
	
	@Override
	protected void onDestroy() {
		super.onStop();
		
		for (DriverConnection connection : connections) {
			connection.unbind(this);
		}

		connections.clear();
		dbHelper.closeDatabases();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent result) {

		if (requestCode == REQUEST_CHOOSE_D40_DEVICE
				&& resultCode == Activity.RESULT_OK) {
			final String d40Address = result
					.getStringExtra(LeanBluetoothPairingInterestingDevices.AVAILABLE_DEVICE_ADDRESS);

			final SharedPreferences prefs = getSharedPreferences(
					ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
			final Editor editor = prefs.edit();
			editor.putString(ForaSettings.D40_BLUETOOTH_ADDRESS, d40Address);
			editor.commit();

		} else if (requestCode == REQUEST_CHOOSE_IR21_DEVICE
				&& resultCode == Activity.RESULT_OK) {
			final String ir21Address = result
					.getStringExtra(BluetoothPairingOneDeviceCheck.DEVICE_ADDRESS);

			final SharedPreferences prefs = getSharedPreferences(
					ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
			final Editor editor = prefs.edit();
			editor.putString(ForaSettings.IR21_BLUETOOTH_ADDRESS, ir21Address);
			editor.commit();

		} else if (requestCode == REQUEST_CHOOSE_IR21_DEVICE
				&& resultCode == Activity.RESULT_CANCELED) {
			Toast.makeText(this, R.string.no_ir21_bluetooth_address,
					Toast.LENGTH_LONG).show();
			setSensorSinkActivityStatus(IR21Sink.ACTION,
					SensorSinkActivityListener.DISCONNECTED);
		} else if (requestCode == REQUEST_CHOOSE_D40_DEVICE
				&& resultCode == Activity.RESULT_CANCELED) {
			Toast.makeText(this, R.string.no_d40_bluetooth_address,
					Toast.LENGTH_LONG).show();
			setSensorSinkActivityStatus(D40Sink.ACTION,
					SensorSinkActivityListener.DISCONNECTED);
		}
	}

	class SaveObservationsTask extends AsyncTask<List<Parcelable>, Void, Void> {

		private DriverConnection connection;

		private BloodPressureDao pressureDao;
		private PulseDao pulseDao;
		private GlucoseDao glucoseDao;
		private TemperatureDao temperatureDao;
		private AmbientDao ambientDao;

		public SaveObservationsTask(DriverConnection connection,
				MGDatabaseHelper dbHelper) {
			this.connection = connection;

			pressureDao = new BloodPressureDao(dbHelper);
			pulseDao = new PulseDao(dbHelper);
			glucoseDao = new GlucoseDao(dbHelper);
			temperatureDao = new TemperatureDao(dbHelper);
			ambientDao = new AmbientDao(dbHelper);
		}

		@Override
		protected Void doInBackground(List<Parcelable>... params) {
			for (Parcelable parcelable : params[0]) {
				GenericObservation value = (GenericObservation) parcelable;

				long typeId = value.getObservationTypeId();
				if (typeId == DriverInterface.TYPE_INDEX_BLOOD_PRESSURE) {
					pressureDao.insert(new BloodPressure(value.getTime(),
							LittleEndian.readInt(value.getValue(), 0),
							LittleEndian.readInt(value.getValue(), 4)));
				} else if (typeId == DriverInterface.TYPE_INDEX_GLUCOSE) {
					glucoseDao.insert(new Glucose(value.getTime(), LittleEndian
							.readInt(value.getValue(), 0), LittleEndian
							.readInt(value.getValue(), 4)));
				} else if (typeId == DriverInterface.TYPE_INDEX_PULSE) {
					pulseDao.insert(new Pulse(value.getTime(), LittleEndian
							.readInt(value.getValue(), 0)));
				} else if (typeId == DriverInterface.TYPE_INDEX_TEMPERATURE) {
					temperatureDao.insert(new Temperature(value.getTime(),
							LittleEndian.readFloat(value.getValue(), 0)));
				} else if (typeId == DriverInterface.TYPE_INDEX_AMBIENT_TEMPERATURE) {
					ambientDao.insert(new Ambient(value.getTime(), LittleEndian
							.readFloat(value.getValue(), 0)));
				}

			}
			return null;
		}

		public void onPostExecute(Void result) {
			setSensorSinkActivityStatus(connection,
					SensorSinkActivityListener.CONNECTED);
		}
	}

	@Override
	public void onDriverStatusChanged(DriverConnection connection, int oldStatus, int newStatus) {
		final String driverAction = connection.getDriverAction();
		Log.d(TAG, String.format("onDriverStatusChanged %s %d", driverAction,
				newStatus));

		newStatus = driverStatusToActivityStatus(oldStatus, newStatus);

		if (oldStatus == SensorSinkActivityListener.DOWNLOADING
			&& newStatus == SensorSinkActivityListener.CONNECTED) {

			// skipping this for now. Will setSensorSinkActionStatus after we
			// refresh the database
			return;
		}

		setSensorSinkActivityStatus(connection, newStatus);
	}

	public void setSensorSinkActivityStatus(DriverConnection connection,
			int newStatus) {
		final String driverAction = connection.getDriverAction();
		Log.d(TAG, String.format("setSensorSinkActivityStatus %s %d",
				driverAction, newStatus));

		final int oldStatus = getSensorStatus(driverAction);

		Log.d(TAG, String.format("old status: %d, new status: %d", oldStatus,
				newStatus));

		if (oldStatus == newStatus) {
			return;
		}

		if (newStatus == SensorSinkActivityListener.CONNECTED) {
			// timer.cancel();
		}

		if (oldStatus != SensorSinkActivityListener.DOWNLOADING
				&& newStatus == SensorSinkActivityListener.CONNECTED) {

			// important! new status have to be set ASAP
			newStatus = SensorSinkActivityListener.DOWNLOADING;
			((SinkDriverConnection) connection)
					.sendReadObservationNumberMessage();
		}

		sensorStates.put(driverAction, newStatus);

		final SensorSinkActivityListener listener = typeListenerMap
				.get(driverAction);
		if (listener != null) {
			listener.onSensorSinkStatusChanged(connection, newStatus);
		}
	}

	/**
	 * Use this function for the cases when DriverConnection is yet to be
	 * created.
	 * 
	 * @param driverAction
	 * @param newStatus
	 */
	public void setSensorSinkActivityStatus(String driverAction, int newStatus) {
		Log.d(TAG, String.format("setSensorSinkActivityStatus %s %d",
				driverAction, newStatus));

		final int oldStatus = getSensorStatus(driverAction);

		Log.d(TAG, String.format("old status: %d, new status: %d", oldStatus,
				newStatus));

		if (oldStatus == newStatus) {
			return;
		}

		sensorStates.put(driverAction, newStatus);

		final SensorSinkActivityListener listener = typeListenerMap
				.get(driverAction);
		if (listener != null) {
			listener.onSensorSinkStatusChanged(null, newStatus);
		}
	}

	@Override
	public void onReceivedMessage(DriverConnection connection, Message msg) {
		Log.d(TAG,
				String.format("onReceivedMessage %s %d",
						connection.getDriverAction(), msg.what));

		int observationNum = msg.arg1;
		switch (msg.what) {
		case SensorSinkService.RESPONSE_CONNECTION_TIMEOUT:
			chooseBtDevice((SinkDriverConnection) connection);

			break;
		
		
		case SensorSinkService.RESPONSE_COUNT_OBSERVATIONS:

			Log.d(TAG, "Sink object number is " + observationNum);

			long[] types = D40Sink.ACTION.equals(connection.getDriverAction()) ? d40Types
					: ir21Types;

			((SinkDriverConnection) connection).sendReadObservations(types, 0,
					observationNum);
			break;

		case SensorSinkService.RESPONSE_READ_OBSERVATIONS:
			final Bundle bundle = msg.getData();
			bundle.setClassLoader(this.getClass().getClassLoader());

			Log.d(TAG, String.format("Received observations"));

			final List<Parcelable> observations = (List<Parcelable>) bundle
					.getParcelableArrayList(SensorSinkService.RESPONSE_FIELD_OBSERVATIONS);
			Log.d(TAG,
					String.format("Received observations from "
							+ connection.getDriverAction()));

			final SaveObservationsTask task = new SaveObservationsTask(
					connection, dbHelper);

			task.execute(observations);
			break;
		}
	}

//	private ConnectionCheckTask connectionCheckTask;

	private DriverConnection addDriverConnection(String driverAction) {

		final SinkDriverConnection driverConnection = new SinkDriverConnection(
				driverAction, clientId);

		driverConnection.addMessagesListener(this);
		driverConnection.addDriverStatusListener(this);

		return driverConnection;
	}

	public DriverConnection findDriverConnectionByDriverAction(
			String driverAction) {
		for (DriverConnection connection : connections) {
			if (driverAction.equals(connection.getDriverAction())) {
				return connection;
			}
		}

		return null;
	}

	public void registerActivityStatusListener(String driverAction,
			SensorSinkActivityListener listener) {
		typeListenerMap.put(driverAction, listener);
	}

	public void unregisterConnectivityStatusListener(String driverAction) {
		typeListenerMap.remove(driverAction);
	}

	@Override
	public int getSensorStatus(String driverAction) {

		if (!sensorStates.containsKey(driverAction)) {
			Log.d(TAG, "Driver " + driverAction + " status: "
					+ SensorSinkActivityListener.DISCONNECTED);
			return SensorSinkActivityListener.DISCONNECTED;

		}
		int status = sensorStates.get(driverAction);

		Log.d(TAG, "getSensorStatus (" + driverAction + ") = " + status);

		return status;
	}

	public void refreshData(String driverAction) {

		final SinkDriverConnection connection = (SinkDriverConnection) findDriverConnectionByDriverAction(driverAction);

		setSensorSinkActivityStatus(connection,
				SensorSinkActivityListener.DOWNLOADING);
		connection.sendReadObservationNumberMessage();
	}


	private void chooseBtDevice(SinkDriverConnection connection) {
		Log.d(TAG, "chooseBtDevice");

		final boolean d40 = D40Sink.ACTION.equals(connection.getDriverAction());
		
		final int requestCode = d40 ? REQUEST_CHOOSE_D40_DEVICE : REQUEST_CHOOSE_IR21_DEVICE;
		
		
		final Intent settings = new Intent(this,
				LeanBluetoothPairingInterestingDevices.class);
		settings.putExtra(
				LeanBluetoothPairingInterestingDevices.DRIVER_ACTION,
				connection.getDriverAction());
		
		settings.putExtra(
				LeanBluetoothPairingInterestingDevices.INTERESTING_DEVICE_NAME_PREFIX,
				fORA_DEVICES_PREFIX);
		startActivityForResult(settings, requestCode);
	}

	public int driverStatusToActivityStatus(int oldStatus, int driverStatus) {

		switch (driverStatus) {
		case DriverStatusListener.UNBOUND:
		case DriverStatusListener.BOUND:
			return oldStatus != SensorSinkActivityListener.CONNECTING ? SensorSinkActivityListener.DISCONNECTED
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

//	class ConnectionCheckTask extends TimerTask 
//		implements DriverStatusListener {
//
//		public final String TAG = ConnectionCheckTask.class.getSimpleName();
//
//		private SinkDriverConnection connection;
//		private boolean wasConnected = false;
//		private String address;
//
//		public ConnectionCheckTask(
//				SinkDriverConnection connection,
//				String address) {
//			this.connection = connection;
//
//			this.address = address;
//
//			connection.addDriverStatusListener(this);
//		}
//
//		@Override
//		public void run() {
//			Log.d(TAG, "run(), " + wasConnected);
//
//			connectionCheckTask = null;
//
//			if (wasConnected) {
//				return;
//			}
//
//			chooseBtDevice(
//					REQUEST_CHOOSE_D40_DEVICE, 
//					address,
//					connection.getDriverAction());
//		}
//
//		@Override
//		public void onDriverStatusChanged(DriverConnection connection, int oldStatus, int newStatus) {
//			if (newStatus == DriverStatusListener.CONNECTED) {
//				wasConnected = true;
//			}
//		}
//	}

	@Override
	public void connect(String driverAction) {
		
		final SinkDriverConnection connection = (SinkDriverConnection) findDriverConnectionByDriverAction(driverAction);
		final SharedPreferences prefs = getSharedPreferences(ForaSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		
		
		if (D40Sink.ACTION.equals(driverAction)) {
			final String d40Address = prefs.getString(ForaSettings.D40_BLUETOOTH_ADDRESS, null);
			
			if (d40Address == null) {
				chooseBtDevice(connection);
				return;
			}
			
			connection.sendStartConnecting(d40Address, CONNECTION_TIMEOUT);

		} else {
			final String ir21Address = prefs.getString(ForaSettings.IR21_BLUETOOTH_ADDRESS, null);
			
			connection.sendStartConnecting(ir21Address);
		}
			
	}

	@Override
	public void disconnect(String driverAction) {
		final SinkDriverConnection connection = (SinkDriverConnection) findDriverConnectionByDriverAction(driverAction);

		if (D40Sink.ACTION.equals(driverAction)) {
			connection.sendDisconnectRequest();
		} else {
			connection.sendDisconnectRequest();
		}
		
	}
}