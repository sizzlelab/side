/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * Maksim Golivkin <maksim@golivkin.eu>
 ******************************************************************************/
package fi.hut.soberit.manager.storage;

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
import android.widget.ListView;
import android.widget.Toast;
import fi.hut.soberit.manager.R;
import fi.hut.soberit.manager.snapshot.ManagerSettings;
import fi.hut.soberit.manager.storage.AvailableStorageListAdapter.ObservationTypeStoragePair;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.core.storage.StorageDao;
import fi.hut.soberit.sensors.core.storagetype.StorageTypeDao;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.generic.StorageType;
import fi.hut.soberit.sensors.storage.GenericObservationStorage;

public class AvailableStorages extends ListActivity {

	protected static final String TAG = AvailableStorages.class.getSimpleName();
	private static final long DISCOVERY_LENGTH = 2000;
	
	private DatabaseHelper dbHelper;
	private StorageDao storageDao;	
	private ObservationTypeDao observationTypeDao;

	private AvailableStorageListAdapter adapter;

	BroadcastReceiver discoveredDriversCallback;
	private IntentFilter actionDiscoveredFilter;
	
	Handler refreshHandler;
	
	private List<Storage> storages = new ArrayList<Storage>();
	
	private List<Long> refreshedStorages = null;
	private ProgressDialog progressDialog;
	

	private EndOfDiscoveryRunnable endOfDiscoveryRunnable;
	private StorageTypeDao storageTypeDao;
	private StorageRegister discoveredStorageCallback;
	public List<StorageType> refreshedStorageTypes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate");
		
		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		observationTypeDao = new ObservationTypeDao(dbHelper);
		
		storageDao = new StorageDao(dbHelper);
		storageTypeDao = new StorageTypeDao(dbHelper);
		
		final List<ObservationType> types = observationTypeDao.getObservationTypes(null, null);
		storages = (ArrayList<Storage>) storageDao.get();
		
		final List<ObservationTypeStoragePair> pairs = new ArrayList<ObservationTypeStoragePair>();
		
		for(Storage storage: storages) {
			final List<StorageType> storageTypePairs = storageTypeDao.get(storage.getId());

			ArrayList<ObservationType> storageTypes = storage.getTypes();
			if (storageTypes == null) {
				storageTypes = new ArrayList<ObservationType>();
				storage.setTypes(storageTypes);
			}

			for(StorageType pair: storageTypePairs) {
				for(ObservationType type: types) {
					if (pair.getObservationTypeId() == type.getId()) {
						storageTypes.add(type);
						pairs.add(new ObservationTypeStoragePair(type, storage));
					}
				}
			}
		}
		
		adapter = new AvailableStorageListAdapter(
				this,
                android.R.layout.simple_list_item_2,
                pairs);
		setListAdapter(adapter);
		
        final ListView listView = getListView();

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
		actionDiscoveredFilter = new IntentFilter();
		actionDiscoveredFilter.addAction(DriverInterface.ACTION_STORAGE_DISCOVERED);
		
		discoveredStorageCallback = new StorageRegister(adapter);
		
		
		refreshHandler = new Handler();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		registerReceiver(discoveredStorageCallback, actionDiscoveredFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		Log.d(TAG, "onPause");
		
		dbHelper.closeDatabases();
		
		if (endOfDiscoveryRunnable != null) {
			endOfDiscoveryRunnable.run();
		}
		
		try {
			unregisterReceiver(discoveredDriversCallback);
		} catch(IllegalArgumentException e) {
			Log.d(TAG, "", e);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = (MenuInflater) getMenuInflater();
		inflater.inflate(R.menu.available_storages_menu, menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.refresh :
			onStorageRefresh();
			break;
		}
		return true;
	}

	private void onStorageRefresh() {
		Log.d(TAG, "onStorageRefresh");

		final SharedPreferences prefs = getSharedPreferences(
				ManagerSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE);
		
		if (prefs.getLong(ManagerSettings.SESSION_IN_PROCESS, -1) != -1) {
			
			Toast.makeText(this, R.string.no_refresh_during_recording, Toast.LENGTH_LONG).show();
			return;
		}
		
		progressDialog = ProgressDialog.show(this,"", getString(R.string.searching_storages));
		
		endOfDiscoveryRunnable = new EndOfDiscoveryRunnable();
		refreshHandler.postDelayed(endOfDiscoveryRunnable, DISCOVERY_LENGTH);
		
		adapter.clear();

		// in case of several refreshes in a row, renew internal list
		storages = (ArrayList<Storage>) storageDao.get();
		
		refreshedStorages  = new ArrayList<Long>();
		refreshedStorageTypes = new ArrayList<StorageType>();
		
		
		final Intent intent = new Intent();
		intent.setAction(DriverInterface.ACTION_START_STORAGE_DISCOVERY);
		
		sendBroadcast(intent);
	}

	
	final class EndOfDiscoveryRunnable implements Runnable {
		
		public void run() {
			if (refreshedStorages == null) {
				return;
			}
			
			storageDao.deleteOtherThan(refreshedStorages);
			storageTypeDao.deleteOtherThan(refreshedStorageTypes);
			
			progressDialog.cancel();
		}
		
	}

	class StorageRegister extends BroadcastReceiver {

		private AvailableStorageListAdapter adapter;

		public StorageRegister(AvailableStorageListAdapter adapter) {
			this.adapter = adapter;
		}		
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "StorageRegister::onReceive");
			if (!DriverInterface.ACTION_STORAGE_DISCOVERED.equals(intent.getAction())) {
				return;
			}
			
			Log.d(TAG, "received ACTION_STORAGE_DISCOVERED");
			
			final Bundle b = intent.getExtras();
			b.setClassLoader(Storage.class.getClassLoader());
			
			final Storage storage = (Storage) b.getParcelable(DriverInterface.INTENT_FIELD_STORAGES);

			final String storageUrl = storage.getUrl();
			final Storage olderStorage = findOlderStorage(storageUrl);
			
			if (olderStorage == null) {
				storage.setId(storageDao.insert(storage));
				storages.add(storage);
			} else {
				storage.setId(olderStorage.getId());
			}
			refreshedStorages.add(storage.getId());
									
			for(ObservationType type: storage.getTypes()) {
				final ObservationType typeFromDb = observationTypeDao.findType(type.getMimeType(), type.getDriver().getUrl());
				
				if (typeFromDb == null) {
					storageDao.delete(storage);
					Toast.makeText(context, R.string.error_registering_storage, Toast.LENGTH_LONG).show();
					return;
				}
				final StorageType storageType = new StorageType(storage.getId(), typeFromDb.getId());
				if (!storageTypeDao.exists(storageType)) {
					storageTypeDao.insert(storageType);
				}
				
				refreshedStorageTypes.add(storageType);
				
				adapter.addItem(new ObservationTypeStoragePair(typeFromDb, storage));
			}
		}

	}
	public Storage findOlderStorage(String url) {
		for(Storage storage: storages) {
			if (storage.getUrl().equals(url)) {
				return storage;
			}
		}
		return null;
	}
}
