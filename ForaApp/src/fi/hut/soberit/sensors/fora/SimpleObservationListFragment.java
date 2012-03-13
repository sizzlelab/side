package fi.hut.soberit.sensors.fora;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.SupportActivity;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.SensorSinkActivityListener;
import fi.hut.soberit.sensors.fora.db.Ambient;
import fi.hut.soberit.sensors.fora.db.AmbientDao;
import fi.hut.soberit.sensors.fora.db.BloodPressure;
import fi.hut.soberit.sensors.fora.db.BloodPressureDao;
import fi.hut.soberit.sensors.fora.db.DatabaseHelper;
import fi.hut.soberit.sensors.fora.db.Glucose;
import fi.hut.soberit.sensors.fora.db.GlucoseDao;
import fi.hut.soberit.sensors.fora.db.Pulse;
import fi.hut.soberit.sensors.fora.db.PulseDao;
import fi.hut.soberit.sensors.fora.db.Record;
import fi.hut.soberit.sensors.fora.db.Temperature;
import fi.hut.soberit.sensors.fora.db.TemperatureDao;

public class SimpleObservationListFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Collection<Record>>, 
		SensorSinkActivityListener, OnRefreshListener {

	private static final int STATUS_INDICATOR_DISCONNECTED = 1;

	private static final int STATUS_INDICATOR_CONNECTING = 2;

	private static final int STATUS_INDICATOR_CONNECTED = 4;

	private static final int STATUS_INDICATOR_DOWNLOADING = 3;

	public static final String TAG = SimpleObservationListFragment.class.getSimpleName();
	
	public static final String DRIVER_ACTION_PARAM = "action";
	
	public static final String TYPES_PARAM = "types";

	public static final int OBSERVATIONS_LOADER_ID = 12346;
	
	ObservationArrayAdapter mAdapter;

	private ForaBrowser activity;

	private PullToRefreshListView pullToRefreshView;

	private TextView emptyView;

	private ProgressBar progressView;

	private TextView statusLine;

	private ListView listView;

	private ImageView statusIndicator;

	private String driverAction;

	private long[] types;
	
	
	@Override
	public void onAttach(SupportActivity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		
		this.activity = (ForaBrowser) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.observation_list, null);
		
		emptyView = (TextView) root.findViewById(android.R.id.empty);
		
		pullToRefreshView = (PullToRefreshListView) root.findViewById(android.R.id.list);
		pullToRefreshView.setOnRefreshListener(this);
		
		listView = pullToRefreshView.getRefreshableView();
		
		progressView = (ProgressBar) root.findViewById(android.R.id.progress);
		
		statusIndicator = (ImageView) root.findViewById(R.id.status_indicator);
		
		statusLine = (TextView) root.findViewById(R.id.status_line);
		
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		mAdapter = new ObservationArrayAdapter(getActivity());
		listView.setAdapter(mAdapter);

		final Bundle bundle = getArguments();
		
		setHasOptionsMenu(true);	

		driverAction = bundle.getString(DRIVER_ACTION_PARAM);
		types = bundle.getLongArray(TYPES_PARAM);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		Log.d(TAG, "onResume");
		
		int sensorStatus = activity.getSensorStatus(driverAction);
		activity.registerActivityStatusListener(driverAction, this);

		onSensorSinkStatusChanged(null, sensorStatus);
		
		if (sensorStatus != SensorSinkActivityListener.CONNECTED) {
			setListShown(false);

			getLoaderManager().initLoader(OBSERVATIONS_LOADER_ID, getArguments(), this);
		}
		Log.d(TAG, driverAction + " sensorStatus" + sensorStatus);
	}
	
	@Override 
    public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		
		getLoaderManager().destroyLoader(OBSERVATIONS_LOADER_ID);
		
		activity.unregisterConnectivityStatusListener(driverAction);	
	}
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "onCreateOptionsMenu");
		final MenuItem refreshMenuItem = menu.add(
				R.id.observations_fragment_menu, 
				R.id.refresh_menu, 
				Menu.CATEGORY_SYSTEM, 
				R.string.refresh_menu);
		
		refreshMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		refreshMenuItem.setIcon(R.drawable.refresh);
		
		final MenuItem settingsMenuItem = menu.add(
				R.id.observations_fragment_menu, 
				R.id.settings_menu, 
				Menu.CATEGORY_SYSTEM, 
				R.string.settings);
		
		settingsMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		settingsMenuItem.setIcon(R.drawable.settings);

//		final MenuItem findAddress = menu.add(
//				R.id.observations_fragment_menu, 
//				R.id.pair_menu, 
//				Menu.CATEGORY_SYSTEM, 
//				"Pair");
//		
//		findAddress.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//		findAddress.setIcon(R.drawable.settings);		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.settings_menu:
			final Intent settings = new Intent(activity, ForaSettings.class);
			startActivity(settings);
			return true;
			
			
		case R.id.refresh_menu:
			onRefresh();
			return true;
		}
		
		return false;
	}

	@Override
	public Loader<Collection<Record>> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");

		AsyncTaskLoader<Collection<Record>> loader = new ObservationsLoader(
				getActivity(), 
				types);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Collection<Record>> loader, Collection<Record> data) {
		Log.d(TAG, "onLoadFinished " + driverAction);

		mAdapter.clear();

		for (Record record : data) {
			mAdapter.add(record);
		}

		final int sensorStatus = activity.getSensorStatus(driverAction);
		
		Log.d(TAG, String.format("onLoadFinished %d %d", sensorStatus, mAdapter.getCount()));
		boolean noCommunicationInProgress = (sensorStatus == CONNECTED || sensorStatus == DISCONNECTED);
		setListShown(noCommunicationInProgress);
		
		pullToRefreshView.onRefreshComplete();
	}

	public void setListShown(boolean shown) {
		Log.d(TAG, String.format("setListShow %b %d", shown, listView.getAdapter().getCount()));
		if (shown) {
			boolean emptyList = listView.getAdapter().getCount() <= 1;
			emptyView.setVisibility(emptyList ? View.VISIBLE : View.GONE);
			pullToRefreshView.setVisibility(emptyList ? View.GONE : View.VISIBLE);
			listView.setVisibility(emptyList ? View.GONE : View.VISIBLE);
			
			progressView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			pullToRefreshView.setVisibility(View.GONE);
			listView.setVisibility(View.GONE);
			
			progressView.setVisibility(View.VISIBLE);
		}
		

	}
	
	@Override
	public void onLoaderReset(Loader<Collection<Record>> loader) {
		mAdapter.clear();
	}
	
	@Override
	public void onSensorSinkStatusChanged(DriverConnection connection, int newStatus) {
		Log.d(TAG, String.format("onSensorSinkStatusChanged %s %d", driverAction, newStatus));
		
		switch(newStatus) {
		case SensorSinkActivityListener.CONNECTING:
			setListShown(false);
			
			statusIndicator.setImageLevel(STATUS_INDICATOR_CONNECTING);
			statusLine.setText(R.string.connecting);
			
			break;
			
		case SensorSinkActivityListener.CONNECTED:
		{
			statusIndicator.setImageLevel(STATUS_INDICATOR_CONNECTED);
			statusLine.setText(R.string.connected);

			final int sensorStatus = activity.getSensorStatus(driverAction);
			
			if (sensorStatus == SensorSinkActivityListener.DOWNLOADING) {
				getLoaderManager().restartLoader(OBSERVATIONS_LOADER_ID, getArguments(), this);
			} else {
				setListShown(true);
			}
			
			break;
		}
			
		case SensorSinkActivityListener.DOWNLOADING:
			setListShown(false);
			
			statusLine.setText(R.string.downloading_data);
			statusIndicator.setImageLevel(STATUS_INDICATOR_DOWNLOADING);
			break;
		
		case SensorSinkActivityListener.DISCONNECTED: 
		{
			statusLine.setText(R.string.disconnected);

			statusIndicator.setImageLevel(STATUS_INDICATOR_DISCONNECTED);
			
			final int sensorStatus = activity.getSensorStatus(driverAction);
			
			if (sensorStatus != SensorSinkActivityListener.CONNECTED) {
				getLoaderManager().restartLoader(OBSERVATIONS_LOADER_ID, getArguments(), this);
			}
			break;
		}
		}
	}

	@Override
	public void onRefresh() {		
		
		switch(activity.getSensorStatus(driverAction)) {
		case SensorSinkActivityListener.CONNECTING:
		case SensorSinkActivityListener.DOWNLOADING:
			
			Toast.makeText(activity, R.string.communication_in_process, Toast.LENGTH_LONG).show();
			
			return;
			
		case SensorSinkActivityListener.CONNECTED:
			setListShown(false);

			activity.refreshData(driverAction);
			break;
					
		case SensorSinkActivityListener.DISCONNECTED: 
		{	
//    		if (!activity.isSensorPaired(driverAction)) {
//    			return;
//    		}

    		setListShown(false);
			activity.connect(driverAction);
			break;
		}
		}		
	}
}

class ObservationsLoader extends AsyncTaskLoader<Collection<Record>> {

	public static final String TAG = ObservationsLoader.class.getSimpleName();
	
	private BloodPressureDao pressureDao;
	private PulseDao pulseDao;
	private GlucoseDao glucoseDao;
	private TemperatureDao temperatureDao;
	private AmbientDao ambientDao;

	private long[] types;

	private DatabaseHelper dbHelper;

	public ObservationsLoader(Context context, long[] types) {
		super(context);

		this.types = types;

		dbHelper = new DatabaseHelper(context);

		pressureDao = new BloodPressureDao(dbHelper);
		pulseDao = new PulseDao(dbHelper);
		glucoseDao = new GlucoseDao(dbHelper);
		temperatureDao = new TemperatureDao(dbHelper);
		ambientDao = new AmbientDao(dbHelper);
		
	}

	@Override
	public Collection<Record> loadInBackground() {
		Log.d(TAG, "loadInBackground");	

		TreeSet<Record> result = new TreeSet<Record>();

		for (long type : types) {
			
			if (type == DriverInterface.TYPE_INDEX_BLOOD_PRESSURE) {
				result.addAll(pressureDao.getMeasurements());
 
			} else if (type == DriverInterface.TYPE_INDEX_GLUCOSE) {
				result.addAll(glucoseDao.getMeasurements());

			} else if (type == DriverInterface.TYPE_INDEX_PULSE) {
				result.addAll(pulseDao.getMeasurements());

			} else if (type == DriverInterface.TYPE_INDEX_AMBIENT_TEMPERATURE) {

				result.addAll(ambientDao.getMeasurements());
			} else if (type == DriverInterface.TYPE_INDEX_TEMPERATURE) {

				
				result.addAll(temperatureDao.getMeasurements());
			}
		}

		return result;
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}
	
	@Override
	protected void onReset() {
		dbHelper.closeDatabases();
	}
}

class ObservationArrayAdapter extends ArrayAdapter<Record> {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

	LayoutInflater inflater;

	private Context context;

	public ObservationArrayAdapter(Context context) {
		super(context, R.layout.observations_item, android.R.id.text1,
				new ArrayList<Record>());

		inflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.observations_item, parent,
					false);
		}

		final TextView typeView = (TextView) convertView
				.findViewById(R.id.type);
		final TextView timeView = (TextView) convertView
				.findViewById(R.id.time);
		final TextView valuesView = (TextView) convertView
				.findViewById(R.id.values);

		final Record item = getItem(position);

		if (item instanceof Ambient) {
			typeView.setText(R.string.ambient);
			timeView.setText(R.string.unknown);
	
			final Ambient ambient = (Ambient) item;
			valuesView.setText(context.getString(R.string.ambient_values,
					ambient.getTemperature()));
		} else if (item instanceof Temperature) {
			typeView.setText(R.string.temperature);
			timeView.setText(R.string.unknown);

			final Temperature ambient = (Temperature) item;
			valuesView.setText(context.getString(R.string.temperature_values,
					ambient.getTemperature()));
		} else if (item instanceof BloodPressure) {
			typeView.setText(R.string.blood_pressure);
			timeView.setText(dateFormat.format(item.getTime()));

			final BloodPressure bloodPressure = (BloodPressure) item;
			valuesView.setText(context.getString(
					R.string.blood_pressure_values,
					bloodPressure.getSystolic(), bloodPressure.getDiastolic()));
		} else if (item instanceof Glucose) {
			typeView.setText(R.string.glucose);
			timeView.setText(dateFormat.format(item.getTime()));

			valuesView.setText(context.getString(R.string.glucose_values,
					((Glucose) item).getGlucose()));
		} else if (item instanceof Pulse) {
			typeView.setText(R.string.pulse);
			timeView.setText(dateFormat.format(item.getTime()));

			valuesView.setText(context.getString(
					R.string.pulse_values,
					((Pulse) item).getPulse()));
		}

		return convertView;
	}
}