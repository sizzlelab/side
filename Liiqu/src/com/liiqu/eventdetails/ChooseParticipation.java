package com.liiqu.eventdetails;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.liiqu.R;
import com.liiqu.db.DatabaseHelper;
import com.liiqu.response.ResponseDao;
import com.liiqu.util.ui.WebViewHelper;


public class ChooseParticipation extends Activity {

	public static final String TAG = ChooseParticipation.class.getSimpleName();
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "name";
	public static final String USER_PICTURE = "pic";
	public static final String USER_CHOICE = "choice";
	public static final String TAB = "tab";
	
	public static final String EVENT_ID = "event_id";
	
	private WebView webView;
	private String name;
	private String picture;
	private long userId;
	private ViewGroup progressContainer;
	private int tabIndex;
	private long eventId;
	private String status;
	private DatabaseHelper dbHelper;
	private ResponseDao responseDao;

	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.choose_participation);		
		
		dbHelper = new DatabaseHelper(this);
		responseDao = new ResponseDao(dbHelper);
		
		progressContainer = (ViewGroup) findViewById(R.id.progress_container);
		
		if (getIntent() != null) {
			final Bundle data = getIntent().getExtras();
			
			userId = data.getLong(USER_ID);
			eventId = data.getLong(EVENT_ID);
			
			
			name = data.getString(USER_NAME);
			picture = data.getString(USER_PICTURE);
			tabIndex = data.getInt(TAB);
			
		} else {
			userId = sis.getLong(USER_ID);
			eventId = sis.getLong(EVENT_ID);
			
			name = sis.getString(USER_NAME);
			picture = sis.getString(USER_PICTURE);
			tabIndex = sis.getInt(TAB);
		}
		
        webView = (WebView) findViewById(R.id.webview); 
		WebViewHelper.setup(webView, this, TAG, "choose_participation.html");
	}
	
	public void onSaveInstanceState(Bundle sis) {
		super.onSaveInstanceState(sis);
		
		sis.putLong(USER_ID, userId);
		sis.putLong(EVENT_ID, eventId);
		sis.putString(USER_NAME, name);
		sis.putString(USER_PICTURE, picture);
		sis.putInt(TAB, tabIndex);
	}
	
	public void onJSParticipanceChoice(String status) {
		Log.d(TAG, String.format("onJSParticipanceChoice(%s)", status));

		this.status = status;
		
		new ParticipationUpdateTask(this, responseDao).execute(
				String.valueOf(eventId),
				String.valueOf(userId),
				status);
	}

	public void returnChoice() {
		final Intent intent = new Intent();
		intent.putExtra(USER_ID, userId);
		intent.putExtra(EVENT_ID, eventId);
		intent.putExtra(USER_CHOICE, status);
		intent.putExtra(TAB, tabIndex);
		
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
	
	public String getUserName() {
		return name;
	}
	
	public String getUserPicture() {
		return picture;
	}
	
    public void onFinishedLoading() {
    	Log.d(TAG, "onFinishedLoading");
    	
    	showProgress(false);
    }

	public void showProgress(final boolean show) {
		runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			progressContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    			webView.setVisibility(show ? View.GONE : View.VISIBLE);
    		}
    	});
	}
}
