package fi.hut.soberit.manager.snapshot;

import java.util.ArrayList;
import java.util.List;

import fi.hut.soberit.manager.Core;
import fi.hut.soberit.manager.R;
import fi.hut.soberit.manager.R.layout;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationType;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class Snapshot extends ListActivity implements OnClickListener {

	private static final String TAG = Snapshot.class.getSimpleName();
	private SessionDao sessionDao;
	private DatabaseHelper dbHelper;
	private ObservationTypeDao observationTypeDao;
	private DriverDao driverDao;
	private ObservationKeynameDao observationKeynameDao;
	private LastObservationListAdapter adapter;

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
        
		final List<ObservationType> types = observationTypeDao.getEnabledObservationTypes();
		final List<GenericObservation> values = new ArrayList<GenericObservation>();
		
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
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick");
		adapter.toggleSelected(position);
	}

	@Override
	public void onClick(View v) {
		stopMainService();
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
}

