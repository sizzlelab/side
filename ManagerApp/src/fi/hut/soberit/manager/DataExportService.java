package fi.hut.soberit.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;


import eu.mobileguild.utils.ThreadUtil;
import fi.hut.soberit.manager.snapshot.ManagerSettings;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.SessionDao;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DataExportService extends Service {

	private static final String TAG = DataExportService.class.getSimpleName();

	public static final String SEND_BY_EMAIL ="email";

    private int NOTIFICATION = R.string.export_started;
	
	private static String rootPackage;
    
	private static final String FILES_FOLDER = "/Sensors/";

	private static String currentDatabaseFolder;

	private static final SimpleDateFormat exportFolderFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss/");
	
	private static final SimpleDateFormat exportFileFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");

	private static final String DATA_EXPORT_ONGOING = "export ongoing";
	
	private File exportLocation;

	private File data;

	private Thread dataExportThread;	
	
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        rootPackage = getApplication().getPackageName();
        currentDatabaseFolder = "data/" + rootPackage + "/databases/";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);

        
        dataExportThread = new Thread(new DataExportThread());
        dataExportThread.start();
        
        final Notification notification = new Notification(
        		R.drawable.ic_icon_export,
        		getString(NOTIFICATION),
                System.currentTimeMillis());
        
        //final Intent appIntent = new Intent(this, SIDE.class);
		final PendingIntent contentIntent =
			PendingIntent.getActivity(this, 0, null, 0);

        notification.setLatestEventInfo(
        		this, 
        		getString(R.string.app_name), 
        		getString(NOTIFICATION), 
        		contentIntent);
        
		notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        
        notificationManager.notify(NOTIFICATION, notification);
        
        final SharedPreferences prefs = getSharedPreferences(
        		ManagerSettings.APP_PREFERENCES_FILE, 
        		MODE_PRIVATE);
        final Editor edit = prefs.edit();
        edit.putBoolean(DATA_EXPORT_ONGOING, true);
        edit.commit();
        
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	Log.d(TAG, "DataExportService::onDestroy called");
    	if (dataExportThread.isAlive()) {
    		dataExportThread.interrupt();
    		Toast.makeText(DataExportService.this, R.string.export_finished, Toast.LENGTH_SHORT).show();
    	}
    	
        final SharedPreferences prefs = getSharedPreferences(
        		ManagerSettings.APP_PREFERENCES_FILE, 
        		MODE_PRIVATE);
        final Editor edit = prefs.edit();
        edit.putBoolean(DATA_EXPORT_ONGOING, false);
        edit.commit();
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class DataExportThread implements Runnable {

		@Override
		public void run() {
			try {
				data = Environment.getDataDirectory();
				
				exportLocation = new File(getExportLocationDirectory(), exportFolderFormat.format(new Date()));
				exportLocation.mkdirs();
				
				exportDatabase();
				
				exportShards();
			} catch(IOException io) {	
				io.printStackTrace();
			} catch(InterruptedException ie) {
			}  finally {
		        notificationManager.cancel(NOTIFICATION);
		        DataExportService.this.stopSelf();
			}
		}

	}	
	
	public static boolean canExport() {

		final File sdCard = Environment.getExternalStorageDirectory();
		
		return sdCard.canWrite(); 
	}
	
	public void exportDatabase() throws IOException, InterruptedException {
    	Log.d(TAG, "Started exploring database");

		ThreadUtil.throwIfInterruped();
		
		final File currentDB = new File(data, currentDatabaseFolder + DatabaseHelper.DATABASE_NAME);
		final File backupDB = new File(exportLocation, "sessions.sqlite");

		final FileChannel src = new FileInputStream(currentDB).getChannel();
		final FileChannel dst = new FileOutputStream(backupDB).getChannel();

		dst.transferFrom(src, 0, src.size());

		src.close();
		dst.close();
	}

	public void exportShards() throws IOException, InterruptedException {
		final DatabaseHelper dbHelper = new DatabaseHelper(DataExportService.this);
		
		try {
			final SessionDao sessionsDao = new SessionDao(dbHelper);
			
			final Cursor c = sessionsDao.getSessions();
	
			for(int i = 0; i<c.getCount(); i++) {
				
				ThreadUtil.throwIfInterruped();
				
				c.moveToPosition(i);
				final long sessionId = c.getLong(c.getColumnIndexOrThrow("session_id"));
				Log.d(TAG, "Exporting shard " + sessionId);
								
				final String exportedShardName = exportedShardName(c);
				
				final File currentDB = new File(data, currentDatabaseFolder + DatabaseHelper.shardName(sessionId));
				final File exportDb = new File(exportLocation, exportedShardName);
	
				try {
					final FileChannel src = new FileInputStream(currentDB).getChannel();
					final FileChannel dst = new FileOutputStream(exportDb).getChannel();
		
					dst.transferFrom(src, 0, src.size());
		
					src.close();
					dst.close();
				} catch(FileNotFoundException ex) {
					Log.d(TAG, "", ex);
					continue;
				}
				
			}
			
			c.close();
		} catch(Exception e) {
			Log.d(TAG, "", e);

		} finally {
			
			
			dbHelper.close();	
		}
	}
	

	private String exportedShardName(Cursor c) {
		final String startDateString = c.getString(c.getColumnIndex("start"));
		final String endDateString = c.getString(c.getColumnIndex("end"));
		
		
		final StringBuilder builder = new StringBuilder();
		builder.append(exportFileFormat.format(DatabaseHelper.getDateFromUtcDateString(startDateString)));
		if (endDateString != null) {
			builder.append("--");
			builder.append(exportFileFormat.format(DatabaseHelper.getDateFromUtcDateString(endDateString)));
		}
		
		builder.append(".sqlite");
		
		return builder.toString();
	}

	public static File getExportLocationDirectory() {
		return new File(
				Environment.getExternalStorageDirectory(), 
				FILES_FOLDER);
	}
}