package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import eu.mobileguild.utils.ThreadUtil;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ToggleButton;

public class SIDE extends Activity implements OnClickListener {

	public static final String TAG = SIDE.class.getSimpleName();
	
	private ArrayAdapter<String> adapter = null;
	
	private DatabaseHelper dbHelper;

	private ObservationTypeDao observationTypeDao;	

	private ObservationKeynameDao observationKeynameDao;
	
	BroadcastReceiver discoveredDriversCallback = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!DriverInterface.ACTION_DISCOVERED.equals(intent.getAction())) {
				return;
			}
			
			final Bundle b = intent.getExtras();
			b.setClassLoader(ObservationType.class.getClassLoader());
			
			final ObservationType type = (ObservationType) b.getParcelable(DriverInterface.INTENT_DATA_TYPE);
			
			final long typeId = observationTypeDao.updateType(type);
			
			observationKeynameDao.updateKeynames(typeId, type.getKeynames());
						
			final long driverId = driverDao.insertDriver(b.getString(DriverInterface.INTENT_DRIVER_SERVICE_URL));

			driverDao.insertEnabledDriver(driverId, new long[] {typeId});
			
			adapter.add(type.getName());
		}
	};

	private DriverDao driverDao;

	private SessionDao sessionDao;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
        setContentView(R.layout.main);

		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		observationTypeDao = new ObservationTypeDao(dbHelper);
		observationKeynameDao = new ObservationKeynameDao(dbHelper);

		driverDao = new DriverDao(dbHelper);
        
        final Button but = (Button) findViewById(R.id.discover_button);
        
        but.setOnClickListener(this);
    
        final ListView list = (ListView) findViewById(R.id.drivers_list);
        adapter = new ArrayAdapter<String>(this, R.layout.driver_list_item);
		list.setAdapter(adapter);
        
		driverDao.cleanDrivers();
		driverDao.cleanEnabledDrivers();
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(DriverInterface.ACTION_DISCOVERED);
		
		registerReceiver(discoveredDriversCallback, filter);
		
		final ToggleButton button = (ToggleButton) findViewById(R.id.service_button);
		button.setOnClickListener(this);
	
    }

	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		
		if (v.getId() == R.id.discover_button) {
			final Intent intent = new Intent();
			intent.setAction(DriverInterface.ACTION_START_DISCOVERY);
			
			sendBroadcast(intent);
			return;
		}
		
		if (v.getId() == R.id.service_button) {
			if (((ToggleButton) v).isChecked()) {
				startMainService();
			} else {
				stopMainService();
			}
		}
	}

	private void startMainService() {
		
		final long sessionId = sessionDao.insertSession(System.currentTimeMillis());
		
		final Intent intent = new Intent(this, Core.class);
		intent.putExtra(Core.INTENT_SESSION_ID, sessionId);
		
		startService(intent);
	}
	
	private void stopMainService() {
		final Intent intent = new Intent(this, Core.class);
		
		stopService(intent);
	}

	@Override
	public void onPause() {
		super.onPause();
		
		unregisterReceiver(discoveredDriversCallback);
	}
}