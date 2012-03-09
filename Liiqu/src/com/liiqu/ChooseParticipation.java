package com.liiqu;

import com.liiqu.util.ui.WebViewHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ChooseParticipation extends Activity {

	public static final String TAG = ChooseParticipation.class.getSimpleName();
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "name";
	public static final String USER_PICTURE = "pic";
	public static final String USER_CHOICE = "choice";
	public static final String TAB = "tab";
	
	private WebView webView;
	private String name;
	private String picture;
	private String userId;
	private ViewGroup progressContainer;
	private int tabIndex;

	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.choose_participation);		
		
		progressContainer = (ViewGroup) findViewById(R.id.progress_container);
		
		if (getIntent() != null) {
			final Bundle data = getIntent().getExtras();
			
			userId = data.getString(USER_ID);
			
			name = data.getString(USER_NAME);
			picture = data.getString(USER_PICTURE);
			tabIndex = data.getInt(TAB);
		} else {
			userId = sis.getString(USER_ID);
			
			name = sis.getString(USER_NAME);
			picture = sis.getString(USER_PICTURE);
			tabIndex = sis.getInt(TAB);
		}
		
        webView = (WebView) findViewById(R.id.webview); 
		WebViewHelper.setup(webView, this, TAG, "choose_participation.html");
	}
	
	public void onSaveInstanceState(Bundle sis) {
		super.onSaveInstanceState(sis);
		
		sis.putString(USER_ID, userId);
		sis.putString(USER_NAME, name);
		sis.putString(USER_PICTURE, picture);
		sis.putInt(TAB, tabIndex);
	}
	
	public void onParticipanceChoice(String choice) {
		Log.d(TAG, String.format("onParticipanceChoice(%s)", choice));
		
		final Intent intent = new Intent();
		intent.putExtra(USER_ID, userId);
		intent.putExtra(USER_CHOICE, choice);
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
    	
    	runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			progressContainer.setVisibility(View.GONE);
    			webView.setVisibility(View.VISIBLE);
    		}
    	});
    }
}
