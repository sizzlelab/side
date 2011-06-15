package fi.hut.soberit.manager.snapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.hut.soberit.manager.Core;
import fi.hut.soberit.manager.ICore;
import fi.hut.soberit.manager.R;
import fi.hut.soberit.manager.R.layout;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.DriverInfo;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class Snapshot extends ListActivity implements OnClickListener {

	private static final String TAG = Snapshot.class.getSimpleName();
	private static final long REFRESH_FREQUENCY = 1000;
	private SessionDao sessionDao;
	private DatabaseHelper dbHelper;
	private ObservationTypeDao observationTypeDao;
	private DriverDao driverDao;
	private ObservationKeynameDao observationKeynameDao;
	private LastObservationListAdapter adapter;
	private Handler refreshTimeHandler = new Handler();
	private RefreshRunnable refreshRunnable = new RefreshRunnable();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.snapshot);
		
		Log.d(TAG, "onCreate");

		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		observationTypeDao = new ObservationTypeDao(dbHelper);
		observationKeynameDao = new ObservationKeynameDao(dbHelper);

		sessionDao = new SessionDao(dbHelper);
		
		if (savedInstanceState == null) {
			
			startMainService();
		}

		driverDao = new DriverDao(dbHelper);
		
		// driver ids are used to filter observation types for only existing drivers, as
		// when driver list is refreshed, observation_types & etc are preserved.
		final ArrayList<Long> ids = new ArrayList<Long>();
		for(DriverInfo info: driverDao.getDriverList()) {
			ids.add(info.getId());
		}
        
		final List<ObservationType> types = observationTypeDao.getObservationTypes(ids, true);
		final Map<Long, GenericObservation> values = new HashMap<Long, GenericObservation>();
		
		for(ObservationType type: types) {
			type.setKeynames(observationKeynameDao.getKeynames(type.getId()));
		}
		
		final boolean[] selected = new boolean [types.size()];
		for(int i = 0; i<types.size(); i++) {

			selected[i] = types.get(i).isEnabled();
		}
		
		adapter = new LastObservationListAdapter(
				this,
                R.layout.driver_list_item,
                types,
                values,
                selected);
		setListAdapter(adapter);
		
        final ListView listView = getListView();

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        final Button stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(this);
        
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
        refreshTimeHandler.postDelayed(refreshRunnable, REFRESH_FREQUENCY);
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
	
		refreshTimeHandler.removeCallbacks(refreshRunnable);
		refreshRunnable.unbind();

		dbHelper.closeDatabases();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick");
		adapter.toggleSelected(position);
	}

	@Override
	public void onClick(View v) {
		stopMainService();
		finish();
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
	
	public class RefreshRunnable implements Runnable, ServiceConnection {

		public ICore coreService;

		private boolean isBound = false;
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Log.d(TAG, "RefreshRunnable");
			
			try {
				if (!isBound) {
					Log.d(TAG, "bounding to main service");
					final Intent coreServiceIntent = new Intent(Snapshot.this, Core.class);
									
				    bindService(coreServiceIntent, this, Context.BIND_AUTO_CREATE);
				    isBound = true;
				    return;
				}
			
				adapter.setValues((Map<Long, GenericObservation>) coreService.getSnapshot());
				adapter.notifyDataSetChanged();
				
			} catch (RemoteException e) {
				e.printStackTrace();
			} finally {
				
				refreshTimeHandler.postDelayed(refreshRunnable, REFRESH_FREQUENCY);	
			}
		}
		
		public void unbind() {
			Log.d(TAG, "unbind");
			
	        unbindService(this);
	        isBound = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected");
			
			coreService = ICore.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected");
			
			coreService = null;
		}
	}
}

