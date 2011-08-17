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
package fi.hut.soberit.manager.snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import fi.hut.soberit.manager.R;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.activities.BroadcastListenerActivity;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;

public class Snapshot extends BroadcastListenerActivity implements OnItemClickListener, OnClickListener {

	private static final String TAG = Snapshot.class.getSimpleName();

	public static final long REFRESH_FREQUENCY = 1000;

	private LastObservationListAdapter adapter;

	final Map<Long, GenericObservation> snapshot 
		= new HashMap<Long, GenericObservation>();	

	@Override
	public void onCreate(Bundle savedInstanceState) {
    	startNewSession = true;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.snapshot);
		
		final boolean[] selected = new boolean [allTypes.size()];
		Arrays.fill(selected, true);
		
		final ListView listView = (ListView) findViewById(android.R.id.list);
				
		adapter = new LastObservationListAdapter(
				this,
                R.layout.driver_list_item,
                allTypes,
                new HashMap<Long, GenericObservation>(),
                selected);
		listView.setAdapter(adapter);
		
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(this);
        
        final Button stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(this);
        
        refreshRunnable = new RefreshRunnable();
        
    	settingsFileName = ManagerSettings.APP_PREFERENCES_FILE;
    	sessionIdPreference = ManagerSettings.SESSION_IN_PROCESS;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		dbHelper.closeDatabases();
	}
	
	@Override
	public void onItemClick(AdapterView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick");
		adapter.toggleSelected(position);
	}

	@Override
	public void onClick(View v) {
		stopSession();
		
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = (MenuInflater) getMenuInflater();
		inflater.inflate(R.menu.snapshot_menu, menu);
		
		return true;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.display :
			onShowData();
			break;
		}
		return true;
	}
	
	private void onShowData() {
        final Intent intent = new Intent(Intent.ACTION_VIEW);

        final ArrayList<ObservationType> selected = new ArrayList<ObservationType>(adapter.getSelectedTypes());       
        
        if (selected.size() == 0) {
        	
        	Toast.makeText(this, R.string.select_any_type, Toast.LENGTH_LONG).show();
        	return;
        }
        
        for(int i = 0; i<selected.size(); i++) {
        	final ObservationType type = selected.get(i);
        	final String mimeType = type.getMimeType();
        	
        	intent.addCategory(mimeType.replace('/', '.'));
        }
        
//        intent.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_DRIVERS, drivers);
        intent.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, selected);

        startActivity(intent);//Intent.createChooser(intent, getString(R.string.display_chooser)));
	}
	
	@Override
	protected void startSession() {
		super.startSession();
		
        refreshHandler.postDelayed(refreshRunnable, REFRESH_FREQUENCY);
	}

	@Override
	protected void bindToOngoingSession() {
		super.bindToOngoingSession();
		
		refreshHandler.postDelayed(refreshRunnable, REFRESH_FREQUENCY);
	}
	
	@Override
	protected void onReceiveObservations(List<Parcelable> observations) {
	
		for(ObservationType type: allTypes) {
			for(Parcelable p: observations) {
				final GenericObservation lastest = (GenericObservation)p;

				if (lastest.getObservationTypeId() == type.getId()) {
					snapshot.put(lastest.getObservationTypeId(), lastest);
					break;
				}
			}
		}
	}
	
	public class RefreshRunnable implements Runnable {

		@Override
		public void run() {
			Log.d(TAG, "RefreshRunnable");
			
			adapter.setValues(snapshot);
			adapter.notifyDataSetChanged();
			
			sessionDao.updateSession(sessionId, System.currentTimeMillis());
			
			refreshHandler.postDelayed(refreshRunnable, REFRESH_FREQUENCY);	
		}
	}
}

