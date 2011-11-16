package fi.hut.soberit.physicalactivity.side;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import eu.mobileguild.WithHttpClient;
import fi.hut.soberit.physicalactivity.R;
import fi.hut.soberit.physicalactivity.Settings;
import fi.hut.soberit.physicalactivity.legacy.LegacyDatabaseHelper;
import fi.hut.soberit.physicalactivity.legacy.LegacyStorage;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SIDEUploadService extends Service implements Runnable {

	// authenticate
	private static final String MOBILE_ID_FIELD = "mobileID";
	private static final String PROJECT_CODE_FIELD = "projectCode";
	private static final String PASSWORD_FIELD = "password";
	private static final String USERNAME_FIELD = "username";

	// upload file messages
	private static final String RESPONSE_STATUS_FIELD = "status";	
	private static final String RESPONSE_MESSAGE_FIELD = "message";

	// upload file fields
	private static final String UPLOAD_FILE_FIELD = "file";
	private static final String ONE_TIME_UPLOAD_ID_FIELD = "oneTimeUploadId";
	
	private static final String TAG = SIDEUploadService.class.getSimpleName();
	
	public static final String FILES_FOLDER = "files folder";
	
	public static final String SESSIONS_API = "session";
	
	public static final String OBSERVATION_FILE_API = "observation_file";

	public static final String SESSION_ID = "session_id";

	private NotificationManager notificationManager;

	private Thread uploadThread;

	private int filesNum;

    private Handler handler = new Handler();
	private Notification progressNotification;
	private String baseUrl;
	private Long sessionId;

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		if (intent == null) {
			return Service.START_FLAG_REDELIVERY;
		}
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		sessionId = intent.getLongExtra(SESSION_ID, -1);
		Log.d(TAG, "uploading session " + sessionId);
		
   		final SharedPreferences prefs = getSharedPreferences(Settings.APP_PREFERENCES_FILE, Context.MODE_PRIVATE);
   		
   		baseUrl = prefs.getString(Settings.SIDE_URL, "") + "/";
   		
   		uploadThread = new Thread(this);
		uploadThread.start();
   			

		return Service.START_FLAG_REDELIVERY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		if (uploadThread != null && uploadThread.isAlive()) {
			uploadThread.interrupt();
		}
		
	}
	
	@Override
	public void run() {
		
		final SharedPreferences prefs = getSharedPreferences(
				Settings.APP_PREFERENCES_FILE, 
				MODE_PRIVATE);

		final Editor editor = prefs.edit();
		editor.putBoolean(Settings.SIDE_UPLOAD_PROCESS_WORKING, true);
		editor.commit();
		
		final HttpClient client = ((WithHttpClient)getApplication()).getHttpClient();	

        try {
			showNotification(getString(R.string.uploading_data));
        	
    		BasicHeader cookieHeader = getCookieHeader(client, prefs);
        	
    		if (cookieHeader == null) {
    			
    			showNotification(getString(R.string.failed_to_connect_to_side));
    			return;
    		}
    		
    		File path = new File (Environment.getDataDirectory() 
    			+ "/data/fi.hut.soberit.physicalactivity/databases/" 
    			+ LegacyDatabaseHelper.getDbName(sessionId));
    		
           	
			showProgressNotification();
    		if (uploadFile(client, cookieHeader, path)) {
    			handler.post(new Runnable() {
    				public void run() {
    					progressNotification.contentView.setProgressBar(R.id.progressBar, filesNum, 100, false);
    			        notificationManager.notify(R.id.upload_progress_notification, progressNotification);
    				}
    			});
    		}
        	
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Log.d(TAG, "uploadFinished");
			
			editor.remove(Settings.SIDE_UPLOAD_PROCESS_WORKING);
			editor.commit();	
			
			notificationManager.cancel(R.id.upload_progress_notification);
		}		
	}	
	
	private boolean uploadFile(final HttpClient client, BasicHeader cookieHeader, File theFile) throws IOException,
			ClientProtocolException, JSONException,
			UnsupportedEncodingException {
		Log.d(TAG, "uploadFile " + theFile);
		
		final String oneTimeUploadId = getUploadId(client, cookieHeader);

		if (oneTimeUploadId == null) {
			return false;
		}
		
		Log.d(TAG, "one time upload id" + oneTimeUploadId);
		
		final HttpPost httpost = new HttpPost(baseUrl + OBSERVATION_FILE_API);
		httpost.addHeader(cookieHeader);

		final MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		multipartEntity.addPart(ONE_TIME_UPLOAD_ID_FIELD, new StringBody(oneTimeUploadId));
		
		multipartEntity.addPart(UPLOAD_FILE_FIELD, new FileBody(theFile));
		httpost.setEntity(multipartEntity);
		
		final HttpResponse fileUploadResponse = client.execute(httpost);
		final String uploadResult = getResponseContent(fileUploadResponse);
		
		if (uploadResult == null) {
			showNotification(getString(R.string.upload_file_error));
			return false;
		}
		
		final JSONObject root = (JSONObject) new JSONTokener(uploadResult).nextValue();
		if (root.has(RESPONSE_MESSAGE_FIELD)) {
			Log.d(TAG, "file uploading failure");

			showNotification(root.getString(RESPONSE_MESSAGE_FIELD));
			return false;
		} 
		
		
		
		return true;
	}

	private String getUploadId(final HttpClient client, BasicHeader cookieHeader)
			throws IOException, ClientProtocolException, JSONException {
		Log.d(TAG, "getUploadId " + baseUrl +"/"+ OBSERVATION_FILE_API + "?step=new");
		
		final HttpGet uploadIdRequest = new HttpGet(baseUrl +"/"+ OBSERVATION_FILE_API + "?step=new");
		uploadIdRequest.addHeader(cookieHeader);
		
		final HttpResponse uploadIdResponse = client.execute(uploadIdRequest);
		final String uploadIdContent = getResponseContent(uploadIdResponse);
		
		if (uploadIdContent == null) {
			showNotification(getString(R.string.upload_id_error));
			return null;
		}
		
		Log.d(TAG, uploadIdContent);
		JSONObject root = (JSONObject) new JSONTokener(uploadIdContent).nextValue();
		if (root.has(RESPONSE_MESSAGE_FIELD)) {
			showNotification(root.getString(RESPONSE_MESSAGE_FIELD));
			return null;
		}
		
		final String oneTimeUploadId = (String) root.get(ONE_TIME_UPLOAD_ID_FIELD);
		return oneTimeUploadId;
	}


	private BasicHeader getCookieHeader(final HttpClient client,
			final SharedPreferences prefs) throws UnsupportedEncodingException,
			IOException, ClientProtocolException {

		final HttpPost sessionRequest = new HttpPost(baseUrl + SESSIONS_API);
		Log.d(TAG, "url " + baseUrl + SESSIONS_API);
		Log.d(TAG, "device " + Build.DEVICE);
		
		Log.d(TAG, "Using username: " + prefs.getString(Settings.SIDE_USERNAME, ""));
		Log.d(TAG, "Using password: " + prefs.getString(Settings.SIDE_PASSWORD, ""));
		Log.d(TAG, "Using project code: " + prefs.getString(Settings.SIDE_PROJECT_CODE, ""));
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair(USERNAME_FIELD, prefs.getString(Settings.SIDE_USERNAME, "")));
		nameValuePairs.add(new BasicNameValuePair(PASSWORD_FIELD, prefs.getString(Settings.SIDE_PASSWORD, "")));
		nameValuePairs.add(new BasicNameValuePair(PROJECT_CODE_FIELD, prefs.getString(Settings.SIDE_PROJECT_CODE, "")));
		nameValuePairs.add(new BasicNameValuePair(MOBILE_ID_FIELD, Build.DEVICE));
		
		sessionRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		final HttpResponse response = client.execute(sessionRequest);
		
		Log.d(TAG, "Response status code " + response.getStatusLine().toString());
		Log.d(TAG, "Response status code " + getResponseContent(response));
		
		Header[] headers = response.getHeaders("Set-Cookie");
		
		String cookieName = null;
		String cookieValue = null;
		
		for(Header header: headers) {
			
			String cookie = header.getValue();
			cookie = cookie.substring(0, cookie.indexOf(";"));
		    cookieName = cookie.substring(0, cookie.indexOf("="));
		    cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
		    
		    if (cookieValue.equals("deleted")) {
		    	continue;
		    }
		}
		
		Log.d(TAG, "Cookie name" + cookieName);
		Log.d(TAG, "Cookie value" + cookieValue);
		
		if (cookieName == null || cookieValue == null) {
			return null;
		}
		
		BasicHeader cookieHeader = new BasicHeader("Cookie", cookieName + "=" + cookieValue + "; has_js=1;");
		return cookieHeader;
	}


	private String getResponseContent(final HttpResponse updateIdResponse) throws IOException {
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

	private void showNotification(String text) {		

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);

		final Notification notification = new Notification(
				R.drawable.ic_icon_hxm, 
				text, 
				System.currentTimeMillis());
		notification.setLatestEventInfo(
				this, 
				getString(R.string.app_name), 
				text, 
				contentIntent);
		
		notificationManager.notify(R.id.upload_msg_notification, notification);
		
	}
	
	private void showProgressNotification() {		
		final String text = getString(R.string.uploading_data);
		progressNotification = new Notification(
				R.drawable.ic_icon_hxm, 
				text, 
				System.currentTimeMillis());
		
		final RemoteViews progressNotificationcontentView = new RemoteViews(this.getPackageName(), R.layout.custom_notification_layout);
		progressNotificationcontentView.setTextViewText(R.id.text, text);       
		progressNotification.contentView = progressNotificationcontentView;
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
		progressNotification.contentIntent = contentIntent;
		progressNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		
		notificationManager.notify(R.id.upload_progress_notification, progressNotification);
		
	}
}
