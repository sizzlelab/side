package fi.hut.soberit.sensors.fora;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.hut.soberit.fora.D40Broadcaster;
import fi.hut.soberit.fora.IR21Broadcaster;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.DriverInterface;
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
import fi.hut.soberit.sensors.generic.ObservationType;

public class SimpleObservationList extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<Record>>, 
		OnClickListener,
		SensorStatusListener {

	public static final String TAG = SimpleObservationList.class.getSimpleName();
	
	public static final String TYPE_PARAM = "type+test";

	ObservationArrayAdapter mAdapter;

	private Button deviceButton;

	private ForaBrowser activity;
	

	private long typeId;

	private int sensorStatus = -1;

	@Override
	public void onAttach(SupportActivity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		
		this.activity = (ForaBrowser) activity;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		// Give some text to display if there is no data. In a real
		// application this would come from a resource.
		setEmptyText(getActivity().getString(R.string.no_observation));
		
		mAdapter = new ObservationArrayAdapter(getActivity());
		setListAdapter(mAdapter);

		final Bundle bundle = getArguments();
		
		setHasOptionsMenu(true);	

		typeId = bundle.getLong(TYPE_PARAM);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		LinearLayout root = new LinearLayout(getActivity());
		root.setOrientation(LinearLayout.VERTICAL);
		

		root.addView(super.onCreateView(inflater, container, savedInstanceState),
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT, 1));

		deviceButton = new Button(getActivity());
		deviceButton.setId(R.id.device_button);
		
		root.addView(deviceButton, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		deviceButton.setOnClickListener(this);
		return root;
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume");

		super.onResume();
		
		sensorStatus = activity.getSensorStatus(typeId);
		activity.registerConnectivityStatusListener(typeId, this);
		Log.d(TAG, typeId + " sensorStatus" + sensorStatus);
		onSensorStatusChanged(null, sensorStatus);
				
		setButtonLabel(sensorStatus);
	}
	
	@Override 
    public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		
		getLoaderManager().destroyLoader((int) typeId);
		
		activity.unregisterConnectivityStatusListener(typeId);	
	}
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "onCreateOptionsMenu");
		
		menu
			.add(R.id.observations_fragment_menu, R.id.settings_menu, Menu.CATEGORY_SYSTEM, R.string.settings_menu)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		final Intent settings = new Intent(activity, ForaSettings.class);
		startActivity(settings);
		return true;
	}

	@Override
	public Loader<List<Record>> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");

		AsyncTaskLoader<List<Record>> loader = new ObservationsLoader(
				getActivity(), args.getLong(TYPE_PARAM));

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Record>> loader, List<Record> data) {
		Log.d(TAG, typeId + "onLoadFinished");

		mAdapter.clear();

		for (Record record : data) {
			mAdapter.add(record);
		}

		setListShown(isResumed() && (sensorStatus == STAND_BY || sensorStatus == SENSOR_DISCONNECTED));
	}

	@Override
	public void onLoaderReset(Loader<List<Record>> loader) {
		mAdapter.clear();
	}
	
	@Override
	public void onClick(View v) {
		if (sensorStatus != SENSOR_DISCONNECTED) {
			activity.stopSession(typeId);
		} else {
			activity.startSession(typeId);
		}
	}

	@Override
	public void onSensorStatusChanged(DriverConnection connection, int newStatus) {
		Log.d(TAG, typeId + " onSensorStatusChanged " + newStatus);
		
		switch(newStatus) {
		case CONNECTING:
		case SENSOR_CONNECTED:
			setListShown(false);
			
			break;
		case DOWNLOADING:			
			setListShown(false);
			break;
			
		case STAND_BY: 
			getLoaderManager().restartLoader((int) typeId, getArguments(), this);
			break;
		
		case SENSOR_DISCONNECTED: 
			getLoaderManager().restartLoader((int) typeId, getArguments(), this);
			break;
		}
		
		setButtonLabel(newStatus);
		sensorStatus = newStatus;
	}
	
	public void setButtonLabel(int newStatus) {
		deviceButton = (Button) getView().findViewById(R.id.device_button);
		switch(newStatus) {
		case CONNECTING:
		case SENSOR_CONNECTED:

			deviceButton.setText(R.string.connecting);
			break;

		case DOWNLOADING:
			deviceButton.setText(R.string.downloading_data);
			break;
			
		case STAND_BY:
			deviceButton.setText(R.string.connected);
			break;
					
		case SENSOR_DISCONNECTED: 
			deviceButton.setText(R.string.disconnected);
			break;
		}		
	}
}

class ObservationsLoader extends AsyncTaskLoader<List<Record>> {

	public static final String TAG = ObservationsLoader.class.getSimpleName();
	
	private long type;

	DatabaseHelper dbHelper;
	private BloodPressureDao pressureDao;
	private PulseDao pulseDao;
	private GlucoseDao glucoseDao;
	private TemperatureDao temperatureDao;
	private AmbientDao ambientDao;

	private ObservationType glucoseType;

	private ObservationType pulseType;

	private ObservationType temperatureType;

	private ObservationType ambientType;

	private Context context;

	private ObservationType pressureType;

	public ObservationsLoader(Context context, long type) {
		super(context);
		this.context = context;
		
		this.type = type;

		dbHelper = new DatabaseHelper(context);

		pressureDao = new BloodPressureDao(dbHelper);
		pulseDao = new PulseDao(dbHelper);
		glucoseDao = new GlucoseDao(dbHelper);
		temperatureDao = new TemperatureDao(dbHelper);
		ambientDao = new AmbientDao(dbHelper);
		
		buildDriverAndUploadersTree();
	}

	protected void buildDriverAndUploadersTree() {
		final D40Broadcaster.Discover foraD40 = new D40Broadcaster.Discover();
	
		for(ObservationType type: foraD40.getObservationTypes(context)) {
			if (DriverInterface.TYPE_BLOOD_PRESSURE.equals(type.getMimeType())) {
				pressureType = type;
			} else if (DriverInterface.TYPE_GLUCOSE.equals(type.getMimeType())) {
				glucoseType = type;
			} else if (DriverInterface.TYPE_PULSE.equals(type.getMimeType())) {
				pulseType = type;
			}			
		}
		
		final IR21Broadcaster.Discover foraIR21 = new IR21Broadcaster.Discover();
		
		for(ObservationType type: foraIR21.getObservationTypes(context)) {
			if (DriverInterface.TYPE_TEMPERATURE.equals(type.getMimeType())) {
				temperatureType = type;
			} else if (DriverInterface.TYPE_AMBIENT_TEMPERATURE.equals(type.getMimeType())) {
				ambientType = type;
			} 			
		}
	}
	
	@Override
	public List<Record> loadInBackground() {
		Log.d(TAG, "loadInBackground");

		ArrayList<Record> result = new ArrayList<Record>();

		if (type == pressureType.getId()) {
			result.addAll(pressureDao.getMeasurements());
		} else if (type == glucoseType.getId()) {

			result.addAll(glucoseDao.getMeasurements());
		} else if (type == pulseType.getId()) {

			result.addAll(pulseDao.getMeasurements());
		} else if (type == temperatureType.getId()) {

			result.addAll(temperatureDao.getMeasurements());
		} else if (type == ambientType.getId()) {
			
			result.addAll(ambientDao.getMeasurements());
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