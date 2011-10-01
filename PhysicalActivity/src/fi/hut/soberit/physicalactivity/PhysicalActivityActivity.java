/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.physicalactivity;

import java.io.File;

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
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.services.BatchDataUploadService;
import fi.hut.soberit.sensors.sessions.SessionsList;

public class PhysicalActivityActivity extends Activity implements OnClickListener {

	private DatabaseHelper dbHelper;
	private SessionDao sessionDao;

	private static final String TAG = PhysicalActivityActivity.class.getSimpleName();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        
		dbHelper = new DatabaseHelper(this);
		dbHelper.getWritableDatabase();
		
		sessionDao = new SessionDao(dbHelper);
		
		final Button activityButton = (Button) findViewById(R.id.start_resume_activity_button);
		activityButton.setOnClickListener(this);

		final Button vitalButton = (Button) findViewById(R.id.start_resume_vital_parameters_button);
		vitalButton.setOnClickListener(this);
		
		final Button sessionsButton = (Button) findViewById(R.id.sessions_button);
		sessionsButton.setOnClickListener(this);

		final Button settingsButton = (Button) findViewById(R.id.settings_button);
		settingsButton.setOnClickListener(this);
		
        final Button batchUpload = (Button) findViewById(R.id.batch_upload_button);
        batchUpload.setOnClickListener(this);
               
        final Button cleanButton = (Button) findViewById(R.id.clean_button);
        cleanButton.setOnClickListener(this);
		
    }
    
    @Override
    public void onResume() {
    	Log.d(TAG, "onResume");
    	super.onResume();
    	
		final Button activityButton = (Button) findViewById(R.id.start_resume_activity_button);
		
		final SharedPreferences prefs = getSharedPreferences(Settings.APP_PREFERENCES_FILE, MODE_PRIVATE);
		long activitySessionId = prefs.getLong(Settings.ACTIVITY_SESSION_IN_PROCESS, -1);
		
		if (activitySessionId != -1) {
			activityButton.setText(R.string.resume_recording_activity_button);
		} else {
			activityButton.setText(R.string.start_recording_activity_button);
		}
    }
    
    @Override
    public void onPause() {
    	Log.d(TAG, "onPause");
    	super.onPause();
    }
    
	@Override
	public void onClick(View v) {
		final SharedPreferences prefs = getSharedPreferences(Settings.APP_PREFERENCES_FILE, MODE_PRIVATE);

		switch(v.getId()) {
		case R.id.start_resume_activity_button:
		{
			final Intent intent = new Intent(this, RecordSession.class);
			
			startActivity(intent);
			
			break;
		}
		case R.id.start_resume_vital_parameters_button:
		{
			final Intent intent = new Intent(this, ForaListenActivity.class);
			
			startActivity(intent);
			
			break;
		}
		case R.id.sessions_button:
		{			
			if (sessionDao.getSessionObjects().size() == 0) {
				Toast.makeText(this, R.string.no_sessions_recorded, Toast.LENGTH_LONG).show();
				break;
			}
			
			final Intent intent = new Intent(this, SessionsList.class);
			
			startActivity(intent);
			
			break;
		}
		case R.id.settings_button:
		{
			final Intent intent = new Intent(this, Settings.class);
			
			startActivity(intent);
			
			break;
		}
		case R.id.batch_upload_button:
			final Intent intent = new Intent(this, BatchDataUploadService.class);
			String folder = "/PhysicalActivity/";
			
			
			File exportLocation = new File(
					Environment.getExternalStorageDirectory(), folder);
			
			if (!exportLocation.canWrite()) {
				Toast.makeText(this, R.string.no_permissions_to_export, Toast.LENGTH_LONG).show();
				return;
			}
			
			if (prefs.getLong(Settings.ACTIVITY_SESSION_IN_PROCESS, -1) != -1) {
				Toast.makeText(this, R.string.cant_export_while_recording, Toast.LENGTH_LONG).show();
				return;	
			} 

			if (prefs.getLong(Settings.VITAL_SESSION_IN_PROCESS, -1) != -1) {
				Toast.makeText(this, R.string.cant_export_while_recording, Toast.LENGTH_LONG).show();
				return;	
			} 

			
			intent.putExtra(BatchDataUploadService.INTENT_FILE_FOLDER, folder);
			startService(intent);		
			break;
			
		case R.id.clean_button:
			Editor editor = prefs.edit();
			editor.remove(Settings.ACTIVITY_SESSION_IN_PROCESS);
			editor.remove(Settings.VITAL_SESSION_IN_PROCESS);
			editor.commit();
			break;
		}
		
	}
}
