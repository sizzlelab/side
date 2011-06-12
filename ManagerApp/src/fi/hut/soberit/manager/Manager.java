package fi.hut.soberit.manager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import fi.hut.soberit.manager.drivers.AvailableDrivers;
import fi.hut.soberit.manager.snapshot.Snapshot;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.ui.Settings;

public class Manager extends Activity implements OnClickListener {

	public static final String TAG = Manager.class.getSimpleName();
	
	private ArrayAdapter<String> adapter = null;
	
	private DatabaseHelper dbHelper;

	private ObservationTypeDao observationTypeDao;	

	private ObservationKeynameDao observationKeynameDao;
	
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
		sessionDao = new SessionDao(dbHelper);
		
		driverDao = new DriverDao(dbHelper);
        
        final Button discoverButton = (Button) findViewById(R.id.drivers_button);
        discoverButton.setOnClickListener(this);
        
        final Button exportButton = (Button) findViewById(R.id.export_button);
        exportButton.setOnClickListener(this);
        
        final Button cleanButton = (Button) findViewById(R.id.clean_button);
        cleanButton.setOnClickListener(this);
		
		final Button button = (Button) findViewById(R.id.start_stop_button);
		button.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		
		if (v.getId() == R.id.drivers_button) {
			final Intent intent = new Intent(this, AvailableDrivers.class);
						
			startActivity(intent);
			return;
		}
		
		if (v.getId() == R.id.start_stop_button) {
			final Intent intent = new Intent(this, Snapshot.class);
			
			startActivity(intent);
			return;
		}
		
		if (v.getId() == R.id.export_button) {
			exportData();
		}
		
		if (v.getId() == R.id.clean_button) {
			final SharedPreferences pref = getSharedPreferences(Settings.APP_PREFERENCES_FILE, MODE_PRIVATE);
			Editor editor = pref.edit();
			editor.remove(Settings.SESSION_IN_PROCESS);
			editor.commit();
		}
	}

	private void exportData() {
		final SharedPreferences prefs = getSharedPreferences(
				Settings.APP_PREFERENCES_FILE,
				MODE_PRIVATE);
		
		
		if (!DataExportService.canExport()) {
			Toast.makeText(this, R.string.no_permissions_to_export, Toast.LENGTH_LONG).show();
			return;
		}
		
		if (prefs.getLong(Settings.SESSION_IN_PROCESS, -1) != -1) {
			Toast.makeText(this, R.string.cant_export_while_recording, Toast.LENGTH_LONG).show();
			return;	
		}
		
		final Intent intent = new Intent(this, DataExportService.class);

		startService(intent);		
	}	

	@Override
	public void onPause() {
		super.onPause();
	}
}