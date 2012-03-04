package com.liiqu;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class ChooseParticipation extends Activity {

	public static final String TAG = ChooseParticipation.class.getSimpleName();
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "name";
	public static final String USER_PICTURE = "pic";
	public static final String USER_CHOICE = "choice";
	
	private WebView webView;
	private String name;
	private String picture;
	private String userId;
	private ViewGroup progressContainer;

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
		} else {
			userId = sis.getString(USER_ID);
			
			name = sis.getString(USER_NAME);
			picture = sis.getString(USER_PICTURE);
		}
		
        webView = (WebView) findViewById(R.id.webview); 
        webView.getSettings().setAllowFileAccess(true); 
        webView.getSettings().setJavaScriptEnabled(true);  

        webView.setWebChromeClient(new WebChromeClient() {
        	  public boolean onConsoleMessage(ConsoleMessage cm) {
        	    Log.d(TAG+"Webview", cm.message() + " -- From line "
        	                         + cm.lineNumber() + " of "
        	                         + cm.sourceId() );
        	    return true;
        	  }
    	});
        
        webView.addJavascriptInterface(this, "Android");
        webView.loadDataWithBaseURL("file://", readAssetsFile("choose_participation.html"), "text/html","utf-8", null);
	}
	
	public void onSaveInstanceState(Bundle sis) {
		super.onSaveInstanceState(sis);
		
		sis.putString(USER_ID, userId);
		sis.putString(USER_NAME, name);
		sis.putString(USER_PICTURE, picture);
	}
	
	public void onParticipanceChoice(String choice) {
		Log.d(TAG, String.format("onParticipanceChoice(%s)", choice));
		
		final Intent intent = new Intent();
		intent.putExtra(USER_ID, userId);
		intent.putExtra(USER_CHOICE, choice);
		
		setResult(Activity.RESULT_OK, intent);
		finish();
		
	}
	
	private String readAssetsFile(String filename) {
		final StringBuilder builder = new StringBuilder();
		
		final AssetManager assets = getAssets();
		
        try {
			final LineNumberReader reader = new LineNumberReader(new InputStreamReader(assets.open(filename)));

			String tmp = null;
			while((tmp = reader.readLine()) != null) {
				builder.append(tmp);
			}
			
			reader.close();
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        Log.d(TAG, "" + builder.toString());
		return builder.toString();
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
