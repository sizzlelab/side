/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import eu.mobileguild.ApplicationWithGlobalPreferences;
import eu.mobileguild.WithHttpClient;
import eu.mobileguild.utils.LittleEndian;
import eu.mobileguild.utils.ThreadUtil;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.ObservationKeynameDao;
import fi.hut.soberit.sensors.ObservationTypeDao;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.core.ObservationValueTable;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Session;

public class BatchDataUploadService extends Service {

	public final String TAG = this.getClass().getSimpleName();

	public  String filesFolder;
	
	private static final String DATA_EXPORT_ONGOING = "export ongoing";

	private File exportLocation;

	private Thread dataExportThread;

	private NotificationManager notificationManager;

	private DatabaseHelper sessionsDbHelper;

	protected SessionDao sessionDao;

	private HashMap<Long, ObservationType> typesMap = new HashMap<Long, ObservationType>();

	private int MAX_PROGRESS = 100;

	protected static final SimpleDateFormat exportFileNameFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

	private String uploadFileField;

	private URI apiUri;
	
	private static final String UPLOAD_RESULT_MESSAGE = "result";
	
	private static final String UPLOAD_RESULT_CODE = "code";

	public static final String INTENT_FILE_FOLDER = "filesFolder";
	
	public static final String INTENT_UPLOAD_FILE_FIELD = "fileField";
	
	public static final String INTENT_API_URL = "apiUrl";
	
	private Notification notification;

	private String prefsFile;

	
	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		prefsFile = ((ApplicationWithGlobalPreferences)getApplication()).getPreferenceFileName();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent == null) {
			return START_FLAG_REDELIVERY;
		}
		
		filesFolder = intent.getStringExtra(INTENT_FILE_FOLDER);
		uploadFileField = intent.getStringExtra(INTENT_UPLOAD_FILE_FIELD);
		apiUri = URI.create(intent.getStringExtra(INTENT_API_URL));

		showBatchUploadNotification(getString(R.string.batch_upload_started), 0, true);

		final SharedPreferences prefs = getSharedPreferences(prefsFile, MODE_PRIVATE);
		final Editor edit = prefs.edit();
		edit.putBoolean(DATA_EXPORT_ONGOING, true);
		edit.commit();

		sessionsDbHelper = new DatabaseHelper(this);
		sessionDao = new SessionDao(sessionsDbHelper);

		final ObservationTypeDao observationTypeDao = new ObservationTypeDao(
				sessionsDbHelper);
		final ObservationKeynameDao observationKeynameDao = new ObservationKeynameDao(
				sessionsDbHelper);

		List<ObservationType> types = observationTypeDao.getObservationTypes(
				null, null);

		for (ObservationType type : types) {
			typesMap.put(type.getId(), type);
			type.setKeynames(observationKeynameDao.getKeynames(type.getId()));
		}
		
		dataExportThread = new Thread(new DataExportThread());
		dataExportThread.start();

		return START_FLAG_REDELIVERY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "DataExportService::onDestroy called");
		if (dataExportThread.isAlive()) {
			dataExportThread.interrupt();
		}

		final SharedPreferences prefs = getSharedPreferences(prefsFile, MODE_PRIVATE);
		final Editor edit = prefs.edit();
		edit.putBoolean(DATA_EXPORT_ONGOING, false);
		edit.commit();
		
		sessionsDbHelper.close();
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
				exportLocation = new File(
						Environment.getExternalStorageDirectory(), filesFolder);
				exportLocation.mkdirs();

				final File file = prepareFile();
				
				final HttpClient client = ((WithHttpClient)BatchDataUploadService.this.getApplication()).getHttpClient();
				
				uploadFile(client, file);
				
			} catch (IOException io) {

				Log.d(TAG, "", io);
			} catch (InterruptedException ie) {
			} catch (JSONException e) {
				Log.d(TAG, "", e);
			} finally {
				BatchDataUploadService.this.stopSelf();
			}
		}
	}

	public File prepareFile() throws IOException, InterruptedException {
		
		try {
			List<Session> sessions = sessionDao.getSessionObjects();
			
			final String exportFileName = exportFileNameFormat.format(new Date()) + ".json";
			
			final File exportFile = new File(exportLocation, exportFileName);
			if (!exportFile.exists()) {
				exportFile.createNewFile();
			}
			final FileWriter destination = new FileWriter(exportFile);

			writeFileHeader(destination, sessions);
			
			Log.d(TAG, "sessions: " + sessions.size());
			
			for(int sessionIndex = 0; sessionIndex < sessions.size(); sessionIndex++) {
				final Session session = sessions.get(sessionIndex);
				
				ThreadUtil.throwIfInterruped();
				
				Log.d(TAG, "Exporting shard " + session.getId());
				writeSession(destination, session, sessionIndex +1 != sessions.size());
								
				final int progress = 75 * (sessionIndex + 1) / sessions.size();
				Log.d(TAG, "progress: " + progress);
				notification.contentView.setProgressBar(R.id.progress_bar, MAX_PROGRESS, progress, false);
				notificationManager.notify(R.id.batch_upload, notification);
			} // for(int sessionIndex = 0; sessionIndex < sessions.size(); sessionIndex++) {
			
			writeFileFooter(destination, sessions);
			destination.close();

			return exportFile;
		} finally {
				
		}
	}

	protected void writeFileHeader(FileWriter destination, List<Session> sessions)  throws IOException {
		destination.write('{');
		
		if (sessions.size() > 0) {
			destination.write("\"sessions\": [");
		}	
	}
	
	protected void writeSessionHeader(final FileWriter destination,
			final Session session, Cursor rowCursor) throws IOException {
		destination.write(String.format("{\"session\": {\"id\": \"%d\", \"start\": \"%s\", \"end\": \"%s\"}, ",
				session.getId(),
				DatabaseHelper.getUtcDateString(session.getStart()),
				DatabaseHelper.getUtcDateString(session.getEnd())));
		
		if (rowCursor.getCount() != 0) {
			destination.write(" \"values\": [");
		}
	}
	
	protected void writeSession(final FileWriter destination,
			final Session session, boolean lastSession) throws IOException {

		final DatabaseHelper shardHelper = new DatabaseHelper(BatchDataUploadService.this, session.getId()); 
		final SQLiteDatabase db = shardHelper.getReadableDatabase();
		
		final Cursor rowCursor = db.query(DatabaseHelper.OBSERVATION_VALUE_TABLE, 
				null, null, null, 
				null, null, ObservationValueTable.TIME + " ASC");
		
		rowCursor.moveToFirst();

		writeSessionHeader(destination, session, rowCursor);
		
		Log.d(TAG, "sessions: " + rowCursor.getCount());
		final StringBuilder builder = new StringBuilder();
		for(int row = 0; row < rowCursor.getCount(); row++) {
			rowCursor.moveToPosition(row);
			
			final GenericObservation observation = ObservationValueTable.observationFromCursor(rowCursor, row);
			
			final ObservationType type = typesMap.get(observation.getObservationTypeId());
			builder.setLength(0);

			writeObservation(builder, session, observation, type, row + 1 != rowCursor.getCount());
			
			destination.write(builder.toString());
		} // for(int row = 0; row < rowCursor.getCount(); row++) {
		
		writeSessionFooter(destination, session, rowCursor);
		
		rowCursor.close();
		shardHelper.close();
		
		if (!lastSession) {
			destination.write(",");
		}
	}
	
	protected void writeSessionFooter(final FileWriter destination,
			final Session session, Cursor rowCursor) throws IOException {
		if (rowCursor.getCount() != 0) {
			destination.write("]} ");
		}		
	}

	// FIXME: Pass FileWriter as a parameter not StringBuilder
	protected void writeObservation(
			final StringBuilder builder,
			Session session, final GenericObservation observation, final ObservationType type, boolean lastObservation) {

		builder.append("{");
		builder.append("\"time\": \"");
		builder.append(DatabaseHelper.dateFormat.format(observation.getTime()));
		builder.append("\", \"type\": \"");
		builder.append(type.getName());
		builder.append("\", ");

		int pos = 0;
		
		byte[] value = observation.getValue();

		final ObservationKeyname[] keynames = type.getKeynames();
		for(int keyname = 0; keyname<keynames.length; keyname++) {
			builder.append('"');
			builder.append(keynames[keyname].getKeyname());
			builder.append("\": \"");		
			
			final String datatype = keynames[keyname].getDatatype();
			if (DriverInterface.KEYNAME_DATATYPE_FLOAT.equals(datatype)) {
				builder.append(LittleEndian.readFloat(value, pos));
				
			} else if (DriverInterface.KEYNAME_DATATYPE_INTEGER.equals(datatype)) {
				builder.append(LittleEndian.readInt(value, pos));							
			}
			pos += 4;

			builder.append("\", ");
		}
		builder.setLength(builder.length() -2);
		builder.append("}");
		
		if (!lastObservation) {
			builder.append(',');
		}
	}

	
	protected void writeFileFooter(FileWriter destination, List<Session> sessions)  throws IOException {
		if (sessions.size() > 0) {
			destination.write("]");
		}
		destination.write('}');	
	}


	private boolean uploadFile(final HttpClient client, File file) throws IOException,
			ClientProtocolException, JSONException,	UnsupportedEncodingException {
		Log.d(TAG, "uploadFile " + file);
				
		final HttpPost httpost = new HttpPost(apiUri);

		final MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		populateMultipartEntity(multipartEntity, file);
		
		httpost.setEntity(multipartEntity);
		
		final HttpResponse fileUploadResponse = client.execute(httpost);
		final String uploadResult = getRequestContent(fileUploadResponse);
		
		if (uploadResult == null) {

			showBatchUploadNotification(getString(R.string.batch_upload_failed), 75, false);
			return false;
		}
		
		final JSONObject root = (JSONObject) new JSONTokener(uploadResult).nextValue();
		
		if (root.has(UPLOAD_RESULT_MESSAGE)) {
			final String message = root.getInt(UPLOAD_RESULT_CODE) == 200
				? getString(R.string.batch_upload_successful)
				: getString(R.string.batch_upload_failed);
			
			showBatchUploadNotification(message, MAX_PROGRESS, false);
			
			return true;
		} 
		
		return false;
	}
	
	protected void populateMultipartEntity(MultipartEntity multipartEntity,
			File file) {
		multipartEntity.addPart(uploadFileField, new FileBody(file));
	}

	private String getRequestContent(final HttpResponse updateIdResponse) throws IOException {
		final StringBuilder builder = new StringBuilder();
		
		final InputStream content = updateIdResponse.getEntity() != null 
			? updateIdResponse.getEntity().getContent()
			: null;
		
		if (content == null) {
			return null;
		}
		final InputStreamReader reader = new InputStreamReader(content);
		char[] buffer = new char[512];
		int res;
		do {
			res = reader.read(buffer);
			builder.append(buffer, 0, res);
		} while(res == 512);
		
		content.close();
		
		return builder.toString();
	}


	private void showBatchUploadNotification(String text, int progress, boolean noClear) {
		if (notification == null) {
			notification = new Notification(
					R.drawable.ic_icon_export, text,
					System.currentTimeMillis());
	
			// final Intent appIntent = new Intent(this, SIDE.class);
			final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					null, 0);
	
			notification.setLatestEventInfo(this, getString(R.string.app_name), text, contentIntent);
			
			RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.batch_upload_notification_layout);
	        contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_icon_export);
	        notification.contentView = contentView;
		}
		
		notification.contentView.setTextViewText(R.id.status_text, text);
		notification.contentView.setProgressBar(R.id.progress_bar, MAX_PROGRESS , progress, false);

		if (noClear) {
			notification.flags |= 
				Notification.FLAG_NO_CLEAR
				| Notification.FLAG_ONGOING_EVENT;
		} else {
			notificationManager.cancel(R.id.batch_upload);

			notification.flags ^= 
				Notification.FLAG_NO_CLEAR
				| Notification.FLAG_ONGOING_EVENT;
		}
		notificationManager.notify(R.id.batch_upload, notification);
	}
}
