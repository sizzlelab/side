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
package fi.hut.soberit.manager.uploaders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import fi.hut.soberit.sensors.UploadedTypeDao;
import fi.hut.soberit.sensors.UploaderDao;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.UploadedType;
import fi.hut.soberit.sensors.generic.Uploader;

public class AvailableUploaders extends ListActivity {

	protected static final String TAG = AvailableUploaders.class.getSimpleName();
	private DatabaseHelper dbHelper;
	private UploaderDao uploaderDao;
	private UploadedTypeDao uploadedTypeDao;

	private AvailableUploaderListAdapter adapter;

	BroadcastReceiver discoveredDriversCallback;
	private IntentFilter actionDiscoveredFilter;
	private HashSet<Driver> drivers = new HashSet<Driver>();
	
	private List<Long> refreshedUploaders;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate");
		
		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		uploaderDao = new UploaderDao(dbHelper);
		uploadedTypeDao = new UploadedTypeDao(dbHelper);
		        		
		final List<Uploader> uploaders = uploaderDao.getUploaders(null);
		
		for(Uploader uploader: uploaders) {
			final List<UploadedType> list = uploadedTypeDao.getTypes(uploader.getId()); 
			
			final UploadedType[] uploadedTypes = new UploadedType[list.size()];
			uploader.setUploadedTypes(list.toArray(uploadedTypes));
		}
		
		adapter = new AvailableUploaderListAdapter(
				this,
                R.layout.uploader_list_item,
                uploaders);
		setListAdapter(adapter);
		
		
        final ListView listView = getListView();

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
		actionDiscoveredFilter = new IntentFilter();
		actionDiscoveredFilter.addAction(DriverInterface.ACTION_UPLOADER_DISCOVERED);

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

		if (refreshedUploaders != null) {			
			uploaderDao.deleteOtherThan(refreshedUploaders);
			uploadedTypeDao.deleteOtherThanBelongingTo(refreshedUploaders);
		}
		
		dbHelper.closeDatabases();
		
		unregisterReceiver(discoveredDriversCallback);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = (MenuInflater) getMenuInflater();
		inflater.inflate(R.menu.available_uploaders_menu, menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.refresh :
			onRefresh();
			break;
		}
		return true;
	}

	private void onRefresh() {
		Log.d(TAG, "onRefresh");

		final SharedPreferences prefs = getSharedPreferences(
				ManagerSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE);
		
		if (prefs.getLong(ManagerSettings.SESSION_IN_PROCESS, -1) != -1) {
			
			Toast.makeText(this, R.string.no_refresh_during_recording, Toast.LENGTH_LONG).show();
			return;
		}
		
		adapter.clear();

		refreshedUploaders  = new ArrayList<Long>();
		
		final Intent intent = new Intent();
		intent.setAction(DriverInterface.ACTION_START_UPLOADER_DISCOVERY);
		
		sendBroadcast(intent);
	}

	
	class DriverRegister extends BroadcastReceiver {

		private AvailableUploaderListAdapter adapter;

		public DriverRegister(AvailableUploaderListAdapter adapter) {
			this.adapter = adapter;
			
		}		
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, intent.getAction());
			
			if (!DriverInterface.ACTION_UPLOADER_DISCOVERED.equals(intent.getAction())) {
				return;
			}
						
			Bundle b = intent.getExtras();
			b.setClassLoader(getClassLoader());
			
			final Uploader uploader = b.getParcelable(DriverInterface.INTENT_FIELD_UPLOADER);
			
			final Uploader existingUploader = uploaderDao.findUploader(uploader.getUrl());
			if (existingUploader != null) {
				uploader.setId(existingUploader.getId());
			} else {
				uploader.setId(uploaderDao.insertUploader(uploader));
			}
			refreshedUploaders.add(uploader.getId());

			
			for (UploadedType type: uploader.getUploadedTypes()) {
				type.setUploaderId(uploader.getId());
				
				final UploadedType existingType = uploadedTypeDao.findType(type.getMimeType(), type.getUploaderId());
				
				if (existingType != null) {
					type.setId(existingType.getId());
				} else {
					type.setId(uploadedTypeDao.insert(type));
				}
			}
			
			adapter.addItem(uploader);

		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick");
		final Uploader type = adapter.toggeEnabled(position);
		
		uploaderDao.updateType(type);
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
