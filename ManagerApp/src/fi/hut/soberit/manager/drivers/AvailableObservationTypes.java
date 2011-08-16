package fi.hut.soberit.manager.drivers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import fi.hut.soberit.manager.R;
import fi.hut.soberit.manager.snapshot.ManagerSettings;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.core.storage.StorageDao;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.storage.GenericObservationStorage;

public class AvailableObservationTypes extends ListActivity {

	protected static final String TAG = AvailableObservationTypes.class.getSimpleName();
	private static final long DISCOVERY_LENGTH = 2000;
	private DriverDao driverDao;
	private DatabaseHelper dbHelper;
	private ObservationTypeDao observationTypeDao;
	private ObservationKeynameDao observationKeynameDao;

	private AvailableObservationTypeListAdapter adapter;

	BroadcastReceiver discoveredDriversCallback;
	private IntentFilter actionDiscoveredFilter;
	
	Handler refreshHandler;
	
	private HashSet<Driver> drivers = new HashSet<Driver>();
	
	private List<Long> refreshedTypes = null;
	private ProgressDialog progressDialog;
	private StorageDao storageDao;
	private Storage defaultStorage;
	private EndOfDiscoveryRunnable endOfDiscoveryRunnable;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate");
		
		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		observationTypeDao = new ObservationTypeDao(dbHelper);
		observationKeynameDao = new ObservationKeynameDao(dbHelper);
		
		driverDao = new DriverDao(dbHelper);
        		
		final List<ObservationType> types = observationTypeDao.getObservationTypes(null, null);
		
		for(ObservationType type : types) {
			final Driver driver = driverDao.getDriverFromType(type.getId());
			type.setDriver(driver);
			drivers.add(driver);
		}
		
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
		
		refreshHandler = new Handler();
		
		storageDao = new StorageDao(dbHelper);
		defaultStorage = storageDao.get(GenericObservationStorage.ACTION);
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
		
		dbHelper.closeDatabases();
		
		if (endOfDiscoveryRunnable != null) {
			endOfDiscoveryRunnable.run();
		}
		
		unregisterReceiver(discoveredDriversCallback);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = (MenuInflater) getMenuInflater();
		inflater.inflate(R.menu.available_drivers_menu, menu);
		
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

		final SharedPreferences prefs = getSharedPreferences(
				ManagerSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE);
		
		if (prefs.getLong(ManagerSettings.SESSION_IN_PROCESS, -1) != -1) {
			
			Toast.makeText(this, R.string.no_refresh_during_recording, Toast.LENGTH_LONG).show();
			return;
		}
		
		progressDialog = ProgressDialog.show(this,"", getString(R.string.searching_drivers));
		
		endOfDiscoveryRunnable = new EndOfDiscoveryRunnable();
		refreshHandler.postDelayed(endOfDiscoveryRunnable, DISCOVERY_LENGTH);
		
		adapter.clear();

		refreshedTypes  = new ArrayList<Long>();
		
		final Intent intent = new Intent();
		intent.setAction(DriverInterface.ACTION_START_DISCOVERY);
		
		sendBroadcast(intent);
	}

	
	final class EndOfDiscoveryRunnable implements Runnable {
		
		public void run() {
			if (refreshedTypes == null) {
				return;
			}
			
			observationTypeDao.deleteOtherThan(refreshedTypes);
			progressDialog.cancel();
		}
		
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
			
			final ObservationType type = (ObservationType) b.getParcelable(DriverInterface.INTENT_FIELD_DATA_TYPE);

			final String driverUrl = type.getDriver().getUrl();
			Driver driver = findOlderDriver(driverUrl);
			
			if (driver == null) {
				driver = type.getDriver();
				driver.setId(driverDao.insertDriver(driverUrl));		
				drivers.add(driver);
			} else {
				type.setDriver(driver);
			}
						
			ObservationType existingType = observationTypeDao.findType(type.getMimeType(), driver.getId());
			if (null == existingType) {
				type.setEnabled(true);
				type.setId(observationTypeDao.insertType(type));
			} else {
				type.setEnabled(existingType.isEnabled());
				type.setId(existingType.getId());
				observationTypeDao.updateType(type);				
			}
			
			refreshedTypes.add(type.getId());
			
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

	public Driver findOlderDriver(String driverUrl) {
		for(Driver info: drivers) {
			if (info.getUrl().equals(driverUrl)) {
				return info;
			}
		}
		return null;
	}
}
