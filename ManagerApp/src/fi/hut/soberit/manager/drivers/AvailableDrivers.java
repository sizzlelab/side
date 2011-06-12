package fi.hut.soberit.manager.drivers;

import java.util.List;

import fi.hut.soberit.manager.R;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class AvailableDrivers extends ListActivity {

	protected static final String TAG = AvailableDrivers.class.getSimpleName();
	private DriverDao driverDao;
	private DatabaseHelper dbHelper;
	private ObservationTypeDao observationTypeDao;
	private ObservationKeynameDao observationKeynameDao;

	private AvailableObservationTypeListAdapter adapter;

	BroadcastReceiver discoveredDriversCallback;
	private IntentFilter actionDiscoveredFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate");
		
		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		observationTypeDao = new ObservationTypeDao(dbHelper);
		observationKeynameDao = new ObservationKeynameDao(dbHelper);
		
		driverDao = new DriverDao(dbHelper);
        
		final List<ObservationType> types = observationTypeDao.getEnabledObservationTypes();
			
		adapter = new AvailableObservationTypeListAdapter(
				this,
                R.layout.driver_list_item,
                types);
		setListAdapter(adapter);
		
		
        final ListView listView = getListView();

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
		actionDiscoveredFilter = new IntentFilter();
		actionDiscoveredFilter.addAction(DriverInterface.ACTION_DISCOVERED);
		
		discoveredDriversCallback = new DriverRegister(adapter); 
				
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		registerReceiver(discoveredDriversCallback, actionDiscoveredFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		Log.d(TAG, "onPause");
		
		unregisterReceiver(discoveredDriversCallback);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = (MenuInflater) getMenuInflater();
		inflater.inflate(R.menu.driver_menu, menu);
				
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.refresh :
			onDriverRefresh();
			break;
		}
		return true;
	}

	private void onDriverRefresh() {
		Log.d(TAG, "onDriverRefresh");
		adapter.clear();
		
		final Intent intent = new Intent();
		intent.setAction(DriverInterface.ACTION_START_DISCOVERY);
		
		sendBroadcast(intent);
	}

	
	class DriverRegister extends BroadcastReceiver {

		private AvailableObservationTypeListAdapter adapter;

		public DriverRegister(AvailableObservationTypeListAdapter adapter) {
			this.adapter = adapter;
			
		}		
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!DriverInterface.ACTION_DISCOVERED.equals(intent.getAction())) {
				return;
			}
			
			Log.d(TAG, "received ACTION_DISCOVERED");
			
			final Bundle b = intent.getExtras();
			b.setClassLoader(ObservationType.class.getClassLoader());
			
			final String driverUrl = b.getString(DriverInterface.INTENT_DRIVER_SERVICE_URL);
			long driverId = driverDao.findDriverId(driverUrl);
			if (-1 == driverId) {
				driverId = driverDao.insertDriver(driverUrl);				
			}
			
			final ObservationType type = (ObservationType) b.getParcelable(DriverInterface.INTENT_DATA_TYPE);
						
			ObservationType existingType = observationTypeDao.findType(type.getMimeType(), driverId);
			if (null == existingType) {
				type.setDriverId(driverId);
				type.setEnabled(true);
				type.setId(observationTypeDao.insertType(type));
			} else {
				type.setEnabled(existingType.isEnabled());
				type.setDriverId(driverId);
				type.setId(existingType.getId());
				observationTypeDao.updateType(type);				
			}
			
			for(ObservationKeyname keyname : type.getKeynames()) {
				final long keynameId = observationKeynameDao.findKeynameId(type.getId(), keyname.getKeyname());
				
				if (keynameId == -1) {
					keyname.setObservationTypeId(type.getId());
					observationKeynameDao.insertKeyname(keyname);
				} else {
					observationKeynameDao.updateKeyname(keyname);
				}
			}
			adapter.addItem(type);
		}
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick");
		final ObservationType type = adapter.toggeEnabled(position);
		
		observationTypeDao.updateType(type);
	}
}
