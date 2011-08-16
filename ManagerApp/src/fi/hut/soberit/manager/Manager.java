package fi.hut.soberit.manager;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import fi.hut.soberit.manager.drivers.AvailableObservationTypes;
import fi.hut.soberit.manager.snapshot.ManagerSettings;
import fi.hut.soberit.manager.snapshot.Snapshot;
import fi.hut.soberit.manager.storage.AvailableStorages;
import fi.hut.soberit.manager.uploaders.AvailableUploaders;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverDao;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.services.BatchDataUploadService;
import fi.hut.soberit.sensors.sessions.SessionsList;

public class Manager extends Activity implements OnClickListener {

	public static final String TAG = Manager.class.getSimpleName();
	
	private DatabaseHelper dbHelper;

	private ObservationTypeDao observationTypeDao;	

	private ObservationKeynameDao observationKeynameDao;
	
	private DriverDao driverDao;

	private SessionDao sessionDao;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
        setContentView(R.layout.main_view);

		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		observationTypeDao = new ObservationTypeDao(dbHelper);
		observationKeynameDao = new ObservationKeynameDao(dbHelper);
		sessionDao = new SessionDao(dbHelper);
		
		driverDao = new DriverDao(dbHelper);

		final Button button = (Button) findViewById(R.id.start_resume_button);
		button.setOnClickListener(this);
		
        final Button discoverButton = (Button) findViewById(R.id.drivers_button);
        discoverButton.setOnClickListener(this);

		final Button uploadersButton = (Button) findViewById(R.id.uploaders_button);
		uploadersButton.setOnClickListener(this);

		final Button sessionsButton = (Button) findViewById(R.id.sessions_button);
		sessionsButton.setOnClickListener(this);
		
		final Button storagesButton = (Button) findViewById(R.id.storages_button);
		storagesButton.setOnClickListener(this);
		
        final Button batchUpload = (Button) findViewById(R.id.batch_upload_button);
        batchUpload.setOnClickListener(this);
        
        final Button exportButton = (Button) findViewById(R.id.export_button);
        exportButton.setOnClickListener(this);
        
        final Button cleanButton = (Button) findViewById(R.id.clean_button);
        cleanButton.setOnClickListener(this);
		
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
		final Button button = (Button) findViewById(R.id.start_resume_button);
		
		final SharedPreferences prefs = getSharedPreferences(ManagerSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		long sessionId = prefs.getLong(ManagerSettings.SESSION_IN_PROCESS, -1);
		
		if (sessionId != -1) {
			button.setText(R.string.resume_recording_button);
		} else {
			button.setText(R.string.start_recording_button);
		}
    }

	@Override
	public void onClick(View v) {
		final SharedPreferences prefs = getSharedPreferences(ManagerSettings.APP_PREFERENCES_FILE, MODE_PRIVATE);

		Log.d(TAG, "onClick");
		switch(v.getId()) {
		case R.id.start_resume_button:
		{
			List<ObservationType> types = observationTypeDao.getObservationTypes(null, true);
			
			if (types.size() == 0) {
				Toast.makeText(this, R.string.no_enabled_types, Toast.LENGTH_LONG).show();
				return;
			}
			
			final Intent intent = new Intent(this, Snapshot.class);
			startActivity(intent);
			break;
		}
		case R.id.drivers_button:
		{
			final Intent intent = new Intent(this, AvailableObservationTypes.class);
						
			startActivity(intent);
			break;
		} 
		case R.id.uploaders_button:
		{
			final Intent intent = new Intent(this, AvailableUploaders.class);
			
			startActivity(intent);
			break;
		}
		case R.id.sessions_button:
		{
			final Intent intent = new Intent(this, SessionsList.class);
			
			startActivity(intent);
			break;
		}
		case R.id.storages_button:
		{
			final Intent intent = new Intent(this, AvailableStorages.class);
			
			startActivity(intent);
			break;
		}
		case R.id.export_button:
		{
			exportData(new Intent(this, DataExportService.class));
			break;
		} 
		case R.id.batch_upload_button:
		{
			final Intent intent = new Intent(this, BatchDataUploadService.class);
			String folder = "/Sensors/tmp";
			
			File exportLocation = new File(
					Environment.getExternalStorageDirectory(), folder);
			
			if (!exportLocation.canWrite()) {
				Toast.makeText(this, R.string.no_permissions_to_export, Toast.LENGTH_LONG).show();
				return;
			}
			
			if (prefs.getLong(ManagerSettings.SESSION_IN_PROCESS, -1) != -1) {
				Toast.makeText(this, R.string.cant_export_while_recording, Toast.LENGTH_LONG).show();
				return;	
			} 

			intent.putExtra(BatchDataUploadService.INTENT_FILE_FOLDER, folder);
			intent.putExtra(BatchDataUploadService.INTENT_API_URL, "http://golivkin.eu/uploader.php");
			intent.putExtra(BatchDataUploadService.INTENT_UPLOAD_FILE_FIELD, "uploadfile");
			break;
		} 
		case R.id.clean_button:
		{
			Editor editor = prefs.edit();
			editor.remove(ManagerSettings.SESSION_IN_PROCESS);
			editor.commit();
			break;
		}
		}
	}

	private void exportData(Intent intent) {
		final SharedPreferences prefs = getSharedPreferences(
				ManagerSettings.APP_PREFERENCES_FILE,
				MODE_PRIVATE);
		
		final File sdCard = Environment.getExternalStorageDirectory();
				
		if (!sdCard.canWrite()) {
			Toast.makeText(this, R.string.no_permissions_to_export, Toast.LENGTH_LONG).show();
			return;
		}
		
		if (prefs.getLong(ManagerSettings.SESSION_IN_PROCESS, -1) != -1) {
			Toast.makeText(this, R.string.cant_export_while_recording, Toast.LENGTH_LONG).show();
			return;	
		} 

		startService(intent);		
	}	

	@Override
	public void onPause() {
		super.onPause();
	}
}