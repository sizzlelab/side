package fi.hut.soberit.physicalactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import fi.hut.soberit.physicalactivity.side.SIDEUploadService;
import fi.hut.soberit.sensors.sessions.SessionsList;

public class PASessionsList extends SessionsList {

	private static final String TAG = PASessionsList.class.getSimpleName();
	private BroadcastReceiver finishedUploadReceiver;

	@Override
	public void onCreate(Bundle icicle) {
		uploadVisible = true;
		
		super.onCreate(icicle);

		Log.d(TAG, "onCreate");
		registerForContextMenu(getListView());
	}

	@Override
	public void onResume() {
		super.onResume();
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(SIDEUploadService.FINISHED_BROADCAST);
				
		final Cursor cursor = this.cursor;
		
		finishedUploadReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "onReceive " + intent.getAction());
				cursor.requery();
			}
		};
		
		registerReceiver(finishedUploadReceiver, filter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		unregisterReceiver(finishedUploadReceiver);
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG, "onCreateContextMenu");
		
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.sessions_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		
		final SharedPreferences prefs = getSharedPreferences(
				Settings.APP_PREFERENCES_FILE, MODE_PRIVATE);

		if (prefs.getLong(Settings.ACTIVITY_SESSION_IN_PROCESS, -1) != -1) {
			Toast.makeText(this, R.string.cant_export_while_recording,
					Toast.LENGTH_LONG).show();
			return false;
		}

		if (prefs.getLong(Settings.VITAL_SESSION_IN_PROCESS, -1) != -1) {
			Toast.makeText(this, R.string.cant_export_while_recording,
					Toast.LENGTH_LONG).show();
			return false;
		}

		if (prefs.getString(Settings.SIDE_URL, "").trim().length() == 0
				|| prefs.getString(Settings.SIDE_PASSWORD, "").trim().length() == 0
				|| prefs.getString(Settings.SIDE_USERNAME, "").trim().length() == 0
				|| prefs.getString(Settings.SIDE_PROJECT_CODE, "").trim()
						.length() == 0) {
			Toast.makeText(this, R.string.cant_upload_without_credentials,
					Toast.LENGTH_LONG).show();
			return false;
		}

		if (prefs.getBoolean(Settings.SIDE_UPLOAD_PROCESS_WORKING, false)) {
			Toast.makeText(this, 
					R.string.uploading_in_progress,
					Toast.LENGTH_LONG).show();
			
			return false;
		}
			
		
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		

		
		final Intent intent = new Intent(this, SIDEUploadService.class);

		Log.d(TAG, "launching upload for " + getListAdapter().getItemId(info.position));
		intent.putExtra(SIDEUploadService.SESSION_ID, getListAdapter().getItemId(info.position));

		startService(intent);

		return true;

	}

}
