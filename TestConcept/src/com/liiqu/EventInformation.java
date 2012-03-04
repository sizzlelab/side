package com.liiqu;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ConnectException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpRequest;
import com.github.droidfu.http.BetterHttpResponse;
import com.liiqu.db.DatabaseHelper;
import com.liiqu.event.Event;
import com.liiqu.event.EventDao;
import com.liiqu.event.EventImageUpdater;
import com.liiqu.response.Response;
import com.liiqu.response.ResponseDao;
import com.liiqu.response.ResponseImageUpdater;

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
                
        final String json = readAssetsFile("match.json");

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
        
		refreshEventInformation();
		
		refreshResponsesInformation();

        final String base = Environment.getExternalStorageDirectory().getAbsolutePath().toString(); 
        Log.d(TAG, "base : " + base);
        
        final String html = readAssetsFile("index.html");

        webView.loadDataWithBaseURL("file://", html, "text/html","utf-8", null);	
    }

	private void refreshEventInformation() {
		try {
			final String eventUrl = LIIQU_EVENT_API + 1091;
			Log.d(TAG, "Requesting: " + eventUrl);
			
			final BetterHttpRequest request = BetterHttp.get(eventUrl);
			final BetterHttpResponse response = request.send();
			
			final JSONParser parser = new JSONParser();
			final JSONObject root = (JSONObject) parser.parse(new InputStreamReader(response.getResponseBody()));
			final JSONObject eventJSON = (JSONObject) root.get("data");
			
			long dbId = (Long) eventJSON.get("id");
			final Event event = new Event();
			event.setLiiquEventId(dbId);
			
			final JSONObject picture = (JSONObject) ((JSONObject) eventJSON.get("owner")).get("picture");
			final String mediumPic = (String) picture.get("medium");
			
			final boolean cached = ImageHtmlLoader.isCached(mediumPic);
			final boolean defaultImage = mediumPic.equals("/static/images/default-team-logo-medium.png"); 

			if (cached) {
    			picture.put("medium", ImageHtmlLoader.getPath(mediumPic));
    		} else {
    			picture.remove("medium");
    		}
			
			eventInfo = eventJSON.toJSONString();
			
			event.setJson(eventInfo);
			eventDao.replace(event);			
			
			if (!cached && !defaultImage) {
				ImageHtmlLoader.start(
						String.valueOf(dbId),
						"owner-pic", 
						mediumPic, 
						eventUpdater, 
						imageLoaderHandler);
			}
			
		} catch (ConnectException ce) {
			Log.d(TAG, "-", ce);
		} catch (IOException ioe) {
			Log.d(TAG, "-", ioe);
		} catch (ParseException pe) {
			Log.d(TAG, "-", pe);
		}
	}
	
	public void refreshResponsesInformation() {
		try {
			final String responseUrl = LIIQU_EVENT_API + 1091 + "/responses";
			Log.d(TAG, "Requesting: " + responseUrl);
	
			final BetterHttpRequest request = BetterHttp.get(responseUrl);
			final BetterHttpResponse response = request.send();
			
			final JSONObject root = (JSONObject) new JSONParser().parse(new InputStreamReader(response.getResponseBody()));
			final JSONArray responsesJSON = (JSONArray) root.get("data");
				
			for (int i = 0; i<responsesJSON.size(); i++) {
		
				final JSONObject responseJSON = (JSONObject) responsesJSON.get(i);
				
				final Response r = new Response();
				
				final String liiquId = (String) responseJSON.get("id");
				r.setLiiquIds(liiquId);				
				
	    		final JSONObject picture = (JSONObject) ((JSONObject) responseJSON.get("user")).get("picture");
				final String mediumPic = (String) picture.get("medium");
	
				final boolean defaultImage = 
						mediumPic.equals("/static/images/default-user-profile-image-medium.png") ||
						mediumPic.equals("/static/images/default-user-profile-image-gray-medium.png");
				
	    		final boolean cached = ImageHtmlLoader.isCached(mediumPic);
				if (cached) {
	    			picture.put("medium", ImageHtmlLoader.getPath(mediumPic));
	    		} else {
	    			picture.remove("medium");
	    		}
	    		
				r.setJson(responseJSON.toJSONString());
	    		responseDao.replace(r);
	
	    		if (!cached && !defaultImage) {
	    			ImageHtmlLoader.start(
	    					liiquId, 
	    					"pic-user-" + r.getLiiquUserId(), 
	    					mediumPic, 
	    					responseUpdater, 
	    					imageLoaderHandler);
	    		}
								
	    		Log.d(TAG, "cached " + cached);
			}		
			
			eventResponses = responsesJSON.toJSONString();
		} catch (ConnectException ce) {
			Log.d(TAG, "-", ce);
		} catch (IOException ioe) {
			Log.d(TAG, "-", ioe);
		} catch (ParseException pe) {
			Log.d(TAG, "-", pe);
		}
	}
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_CHOOSE_PARTICIPATION && resultCode == Activity.RESULT_OK) {
    		final String choice = data.getStringExtra(ChooseParticipation.USER_CHOICE);
    		final String userId = data.getStringExtra(ChooseParticipation.USER_ID);
    		
    		webView.loadUrl(String.format("javascript:window.changeParticipation(\"%s\", \"%s\")", userId, choice));
    	}
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