package com.liiqu;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.liiqu.db.DatabaseHelper;
import com.liiqu.event.EventDao;
import com.liiqu.event.EventImageUpdater;
import com.liiqu.response.ResponseDao;
import com.liiqu.response.ResponseImageUpdater;
import com.liiqu.util.AssetUtil;

public class EventInformation extends Activity {
    private static final int REQUEST_CHOOSE_PARTICIPATION = 1;
	private static final String TAG = EventInformation.class.getSimpleName();
	private static final String LIIQU_EVENT_API = "https://liiqu.com/api/events/";
	private WebView webView;
	private org.json.JSONObject data;
	private ViewGroup progressContainer;
	private DatabaseHelper dbHelper;
	private EventDao eventDao;
	private ImageHtmlLoaderHandler imageLoaderHandler;
	private ResponseDao responseDao;
	private EventImageUpdater eventUpdater;
	private ResponseImageUpdater responseUpdater;
	private String eventResponses;
	private String eventInfo;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_information);
                
        final String json = AssetUtil.readAssetsFile(this, "match.json");

        dbHelper = new DatabaseHelper(this);
        eventDao = new EventDao(dbHelper);
        responseDao = new ResponseDao(dbHelper);
        
        eventUpdater = new EventImageUpdater(eventDao);
        responseUpdater = new ResponseImageUpdater(responseDao);
        
        progressContainer = (ViewGroup) findViewById(R.id.progress_container);
        
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
        
        imageLoaderHandler = new ImageHtmlLoaderHandler(webView);
        
        final String base = Environment.getExternalStorageDirectory().getAbsolutePath().toString(); 
        Log.d(TAG, "base : " + base);
        
        final String html = AssetUtil.readAssetsFile(this, "participants_list.html");

        webView.loadDataWithBaseURL("file://", html, "text/html","utf-8", null);	
    }

    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_CHOOSE_PARTICIPATION && resultCode == Activity.RESULT_OK) {
    		final String choice = data.getStringExtra(ChooseParticipation.USER_CHOICE);
    		final String userId = data.getStringExtra(ChooseParticipation.USER_ID);
    		
    		webView.loadUrl(String.format("javascript:window.changeParticipation(\"%s\", \"%s\")", userId, choice));
    	}
    }


    
    public String getMatchInformation() {
    	
    	return eventInfo; 
    }
    
    public String getMatchResponses() {
    	
    	return eventResponses;
    }
    
    
    public void onChangeMyRsvp() {
    	Log.d(TAG, "onChangeMyRsvp");
    	
    	final Intent intent = new Intent(this, ChooseParticipation.class);
    	intent.putExtra(ChooseParticipation.USER_ID, "right-header");
    	intent.putExtra(ChooseParticipation.USER_NAME, "Maksim Golivkin");
    	intent.putExtra(ChooseParticipation.USER_PICTURE, "https://graph.facebook.com/1540570866/picture?type=square");
    	
    	startActivityForResult(intent, REQUEST_CHOOSE_PARTICIPATION);
    }
    
    public void onChangeRsvp(String uid, String name, String pic) {
    	final Intent intent = new Intent(this, ChooseParticipation.class);
    	intent.putExtra(ChooseParticipation.USER_ID, uid);
    	intent.putExtra(ChooseParticipation.USER_NAME, name);
    	intent.putExtra(ChooseParticipation.USER_PICTURE, pic);
    	
    	startActivityForResult(intent, REQUEST_CHOOSE_PARTICIPATION);    	
    }
    
    public String getParticipation() {
    	Log.d(TAG, "getParticipation() " + "maybe");
    	
    	return "maybe"; 
    }
    
    
    public void openMap(String uri) {
    	Log.d(TAG, "openMap " + uri);
    	
    	final Intent intent = new Intent();
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse(uri));
    	
    	startActivity(intent);
    }
    
    public void onFinishedLoading() {
    	Log.d(TAG, "onFinishedLoading");
    	
    	runOnUiThread(new Runnable() {
    		public void run() {
    	    	progressContainer.setVisibility(View.GONE);
    	    	webView.setVisibility(View.VISIBLE);    			
    		}
    	});
    }
}