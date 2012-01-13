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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.generic.Session;

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
    	
    	dbHelper.closeDatabases();
    }
    
	@Override
	public void onClick(View v) {
		final SharedPreferences prefs = getSharedPreferences(Settings.APP_PREFERENCES_FILE, MODE_PRIVATE);

		if (v.getId() == R.id.start_resume_activity_button) {
			if (Settings.METER_HXM.equals(prefs.getString(Settings.METER, "")) &&
				prefs.getString(Settings.HXM_BLUETOOTH_ADDRESS, null) == null	
					) {
				Toast.makeText(this, R.string.select_hxm_bluetooth, Toast.LENGTH_LONG).show();
				return;
			}
			final Intent intent = new Intent(this, RecordSession.class);
			startActivity(intent);
		} else if (v.getId() == R.id.start_resume_vital_parameters_button) {
			if (prefs.getString(Settings.D40_BLUETOOTH_ADDRESS, null) == null) {
				Toast.makeText(this, R.string.select_d40_bluetooth, Toast.LENGTH_LONG).show();
				return;
			}
			
			if (prefs.getString(Settings.IR21_BLUETOOTH_ADDRESS, null) == null) {
				Toast.makeText(this, R.string.select_ir21_bluetooth, Toast.LENGTH_LONG).show();
				return;
			}
			
			final Intent intent = new Intent(this, VitalParametersActivity.class);
			startActivity(intent);
		} else {
			final List<Session> sessionObjects = sessionDao.getSessionObjects();
			
			if (v.getId() == R.id.sessions_button && sessionObjects.size() == 0) {
					Toast.makeText(this, R.string.no_sessions_recorded, Toast.LENGTH_LONG).show();
			} else if (v.getId() == R.id.sessions_button && sessionObjects.size() > 0) {

				final Intent intent = new Intent(this, PASessionsList.class);
				startActivity(intent);
			} else if (v.getId() == R.id.settings_button) {
				final Intent intent = new Intent(this, Settings.class);
				startActivity(intent);
			} else if (v.getId() == R.id.clean_button) {
				Editor editor = prefs.edit();
				editor.remove(Settings.ACTIVITY_SESSION_IN_PROCESS);
				editor.remove(Settings.VITAL_SESSION_IN_PROCESS);
				editor.commit();
			}
		}
		
	}
}
