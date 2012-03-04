package com.liiqu;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.SupportActivity;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
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
import com.liiqu.util.AssetUtil;


public class EventInfromationFragment extends Fragment 
	implements LoaderManager.LoaderCallbacks<String>
	{

	private static final String JAVASCRIPT_CHANGE_PARTICIPATION = "javascript:window.changeParticipation(\"%s\", \"%s\")";

	public static String TAG = EventInfromationFragment.class.getSimpleName();
	
	private static final int DATABASE_LOADER_ID = 1;
	private static final int INTERNET_LOADER_ID = 2;

	private static final String JAVASCRIPT_UPDATE_DATA = "javascript:jsUpdateData();";

	public static final String EVENT_ID = "event id";

	private ViewGroup progressContainer;

	private WebView webView;

	private ResponseDao responseDao;
	private EventDao eventDao;

	private DatabaseHelper dbHelper;

	private ImageHtmlLoaderHandler imageLoaderHandler;

	private SupportActivity activity;

	private String loaderResult;

	@Override
	public void onAttach(SupportActivity context) {
		super.onAttach(context);
		
		dbHelper = new DatabaseHelper(context.getBaseContext());
		
		eventDao = new EventDao(dbHelper);
		responseDao = new ResponseDao(dbHelper);
		
		activity = context;
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateView");
		
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.event_information, null);
		
		progressContainer = (ViewGroup) root.findViewById(R.id.progress_container);
		webView = (WebView) root.findViewById(R.id.webview);
		
        webView = (WebView) root.findViewById(R.id.webview); 
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
        
        imageLoaderHandler = new ImageHtmlLoaderHandler(webView);
        
        webView.addJavascriptInterface(this, "Android");
        
        final String html = AssetUtil.readAssetsFile((Context) activity, "index.html");
        webView.loadDataWithBaseURL("file://", html, "text/html","utf-8", null);
		
		return root;
	}
	
	@Override
	public void onActivityCreated (Bundle sis) {
		super.onActivityCreated(sis);
		Log.d(TAG, "onActivityCreated");
		
		setHasOptionsMenu(true);
		
		getLoaderManager().initLoader(DATABASE_LOADER_ID, getArguments(), this);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "onCreateOptionsMenu");
		final MenuItem refreshMenuItem = menu.add(
				R.id.event_information_menu, 
				R.id.refresh_menu, 
				Menu.CATEGORY_SYSTEM, 
				R.string.refresh_menu);
		
		refreshMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		refreshMenuItem.setIcon(R.drawable.refresh);		
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override 
    public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		
		getLoaderManager().destroyLoader(DATABASE_LOADER_ID);
		getLoaderManager().destroyLoader(INTERNET_LOADER_ID);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.refresh_menu:
			onRefresh();
			return true;
		}
		
		return false;
	}

	
	private void onRefresh() {
		progressContainer.setVisibility(View.VISIBLE);
		webView.setVisibility(View.GONE);
		
		getLoaderManager().restartLoader(INTERNET_LOADER_ID, getArguments(), this);
	}

	@Override
	public Loader<String> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");
				
		switch(id) {
		case DATABASE_LOADER_ID: return new DatabaseLoader(
				getActivity(), 
				eventDao, responseDao,
				args.getLong(EVENT_ID)
				);
		
		case INTERNET_LOADER_ID: return new InternetLoader(
				getActivity(),
				eventDao, responseDao,
				args.getLong(EVENT_ID),
				imageLoaderHandler
				);
		}
		
		throw new RuntimeException("Shouldn't happen");
	}

	@Override
	public void onLoadFinished(Loader<String> loader, String data) {
		Log.d(TAG, "onLoadFinished");
		
		loaderResult = data;
		
		webView.loadUrl(String.format(JAVASCRIPT_UPDATE_DATA));
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {
		
		loaderResult = null;
	}
	
	public String getJSData() {
		Log.d(TAG, "getJSData()");
		
		return loaderResult;
	}
	
	public void onJSFinishedLoading() {

		activity.runOnUiThread(new Runnable() {
			public void run() {
				progressContainer.setVisibility(View.GONE);
				webView.setVisibility(View.VISIBLE);
			}
		});
	}
	
	public String getJSParticipation() {
		return "maybe";
	}
	
	public void jsOpenMap(String uri) {
		((EventInformation2) activity).openMap(uri);
	}
	
	public void onJSChangeMyParticipation() {
		((EventInformation2) activity).startRsvpActivity(
				"right-header",
				"Maksim Golivkin",
				"https://graph.facebook.com/1540570866/picture?type=square"
				);
	}
	
	public void onJSChangeParticipation(String id, String name, String picture) {
		((EventInformation2) activity).startRsvpActivity(id, name, picture);
	}
	

	public void onChangeParticipation(String userId, String choice) {

		webView.loadUrl(String.format(JAVASCRIPT_CHANGE_PARTICIPATION, userId, choice));
	}
	
	
}

class DatabaseLoader extends AsyncTaskLoader<String> {
	
	public static final String TAG = DatabaseLoader.class.getSimpleName(); 
	
	private EventDao eventDao;
	private ResponseDao responseDao;
	private long liiquEventId;
	
	public DatabaseLoader(Context context, EventDao eventDao, ResponseDao responseDao, long liiquEventId) {
		super(context);
		
		this.liiquEventId = liiquEventId;
		
        this.eventDao = eventDao;
        this.responseDao = responseDao;
	}

	@Override
	public String loadInBackground() {
		Log.d(TAG, "loadInBackground");
		
		final Event event = eventDao.getEvent(liiquEventId);
		
		final ArrayList<Response> responses = responseDao.getResponses(liiquEventId);

		final StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		
		if (event != null) {
			buffer.append("\"event\":");
			buffer.append(event.getJson());
			buffer.append(",");
		} 
		
		buffer.append("\"responses\": [");
		
		if (responses != null) {
			for (Response response: responses) {
				buffer.append(response.getJson());
				buffer.append(",");
			}
			buffer.setLength(buffer.length() -1);
		}
		
		buffer.append("]}");		
		
		Log.d(TAG, buffer.toString());
		
		return buffer.toString();
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
	}

}


class InternetLoader extends AsyncTaskLoader<String> {
	
	private static final String LIIQU_EVENT_API = "https://liiqu.com/api/events/";
	
	private static final String TAG = InternetLoader.class.getSimpleName();
	
	private EventDao eventDao;
	private ResponseDao responseDao;
	private long liiquEventId;

	private ResponseImageUpdater responseUpdater;

	private EventImageUpdater eventUpdater;

	private ImageHtmlLoaderHandler imageLoaderHandler;

	
	public InternetLoader(Context context, EventDao eventDao, ResponseDao responseDao, long liiquEventId, ImageHtmlLoaderHandler imageLoaderHandler) {
		super(context);
		
		this.liiquEventId = liiquEventId;
		
        this.eventDao = eventDao;
        this.responseDao = responseDao;        
        
        this.imageLoaderHandler = imageLoaderHandler;
	
        eventUpdater = new EventImageUpdater(eventDao);
        responseUpdater = new ResponseImageUpdater(responseDao);
	}

	@Override
	public String loadInBackground() {
		Log.d(TAG, "loadInBackground()");
		
		final StringBuffer buffer = new StringBuffer();
		buffer.append("{\"event\":");
		buffer.append(refreshEventInformation(liiquEventId));
		buffer.append(",");
		buffer.append("\"responses\": ");
		buffer.append(refreshResponsesInformation(liiquEventId));
		buffer.append("}");		
		
		return buffer.toString();
	}
	
	private String refreshEventInformation(long id) {
		try {
			final String eventUrl = LIIQU_EVENT_API + id;
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
			
			
			event.setJson(eventJSON.toJSONString());
			eventDao.replace(event);			
			
			if (!cached && !defaultImage) {
				ImageHtmlLoader.start(
						String.valueOf(dbId),
						"owner-pic", 
						mediumPic, 
						eventUpdater, 
						imageLoaderHandler);
			}
			
			return eventJSON.toJSONString();
			
		} catch (ConnectException ce) {
			Log.d(TAG, "-", ce);
		} catch (IOException ioe) {
			Log.d(TAG, "-", ioe);
		} catch (ParseException pe) {
			Log.d(TAG, "-", pe);
		}
		
		return null;
	}
	
	public String refreshResponsesInformation(long id) {
		try {
			final String responseUrl = LIIQU_EVENT_API + id + "/responses";
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
			
			return responsesJSON.toJSONString();
		} catch (ConnectException ce) {
			Log.d(TAG, "-", ce);
		} catch (IOException ioe) {
			Log.d(TAG, "-", ioe);
		} catch (ParseException pe) {
			Log.d(TAG, "-", pe);
		}
		
		return null;
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
	}


}