package com.liiqu.eventdetails;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.facebook.android.R;
import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpRequest;
import com.github.droidfu.http.BetterHttpResponse;
import com.liiqu.IdentityDownloadTask;
import com.liiqu.LiiquPreferences;
import com.liiqu.event.Event;
import com.liiqu.event.EventDao;
import com.liiqu.event.EventImageUpdater;
import com.liiqu.facebook.SessionStore;
import com.liiqu.response.Response;
import com.liiqu.response.ResponseDao;
import com.liiqu.response.ResponseImageUpdater;
import com.liiqu.util.ui.ImageHtmlLoader;
import com.liiqu.util.ui.ImageHtmlLoaderHandler;

class InternetLoader extends AsyncTaskLoader<String> {
		
	private static final String TAG = InternetLoader.class.getSimpleName();

	private static final String LIIQU_EVENT_API = LiiquPreferences.ROOT_URL + "api/events/";
	
	private ResponseImageUpdater responseUpdater;

	private EventImageUpdater eventUpdater;

	private ImageHtmlLoaderHandler imageLoaderHandler;

	IdentityDownloadTask identityTask;

	private Activity activity;

	private long liiquEventId;

	private EventDao eventDao;

	private ResponseDao responseDao; 

	
	public InternetLoader(Activity context, EventDao eventDao, ResponseDao responseDao, long liiquEventId, ImageHtmlLoaderHandler imageLoaderHandler) {
		super(context);
		
		this.activity = context;
		this.liiquEventId = liiquEventId;
		
        this.eventDao = eventDao;
        this.responseDao = responseDao;
        
        this.imageLoaderHandler = imageLoaderHandler;
	
        eventUpdater = new EventImageUpdater(eventDao);
        responseUpdater = new ResponseImageUpdater(responseDao);
        
		identityTask = new IdentityDownloadTask(context);
	}

	@Override
	public String loadInBackground() {
		Log.d(TAG, "loadInBackground()");
		
		final Facebook fbSession = new Facebook(LiiquPreferences.FACEBOOK_APP_ID);
		SessionStore.restore(fbSession, activity);
		
		final int responseCode = identityTask.doInBackground(fbSession);

		if (responseCode == IdentityDownloadTask.CONNECTION_ERROR) {
			
			activity.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(activity, R.string.connection_problem, Toast.LENGTH_LONG).show();
				}
			});
			
			return null;
		} else 
		if (responseCode != IdentityDownloadTask.SUCCESS) {
			
			identityTask.onPostExecute(responseCode);
			return null;
		}
		
		identityTask.onPostExecute(responseCode);
		
		refreshEventInformation(liiquEventId);	
		refreshResponsesInformation(liiquEventId);
		
		return null;
	}
	
	private void refreshEventInformation(long id) {
		try {
			final String eventUrl = LIIQU_EVENT_API + id;
			Log.d(TAG, "Requesting: " + eventUrl);
			
			final BetterHttpRequest request = BetterHttp.get(eventUrl);
			
			final BetterHttpResponse response = request.send();
			Log.d(TAG, "response: " + response.getResponseBodyAsString());
						
			
			final JSONParser parser = new JSONParser();
			final JSONObject root = (JSONObject) parser.parse(new InputStreamReader(response.getResponseBody()));
			final JSONObject eventJSON = (JSONObject) root.get("data");
			
			long dbId = (Long) eventJSON.get("id");
			final Event event = new Event();
			event.setLiiquEventId(dbId);
			
			final JSONObject owner = eventJSON.containsKey("owner")
					? (JSONObject) eventJSON.get("owner")
					: (JSONObject) eventJSON.get("home_team");
					
			final JSONObject picture = (JSONObject) owner.get("picture");
			final String picUrl = (String) picture.get("medium");
			
			final boolean cached = ImageHtmlLoader.isCached(picUrl);
			final boolean defaultImage = picUrl.equals("/static/images/default-team-logo-medium.png"); 

			if (cached) {
    			picture.put("medium", ImageHtmlLoader.getPath(picUrl));
    		} else {
    			picture.remove("medium");
    		}
			
			event.setJson(eventJSON.toJSONString());
			eventDao.replace(event);			
			
			if (!cached && !defaultImage) {
				ImageHtmlLoader.start(
						String.valueOf(dbId),
						"owner-pic", 
						picUrl, 
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
	
	public void refreshResponsesInformation(long id) {
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
				final String picUrl = (String) picture.get("medium");
				
				final boolean defaultImage = 
						picUrl.equals("/static/images/default-user-profile-image-medium.png") ||
						picUrl.equals("/static/images/default-user-profile-image-gray-medium.png");
				
	    		final boolean cached = ImageHtmlLoader.isCached(picUrl);
				if (cached) {
	    			picture.put("medium", ImageHtmlLoader.getPath(picUrl));
	    		} else {
	    			picture.remove("medium");
	    		}
	    		
				r.setJson(responseJSON.toJSONString());
	    		responseDao.replace(r);
	
	    		if (!cached && !defaultImage) {
	    			ImageHtmlLoader.start(
	    					liiquId, 
	    					"pic-user-" + r.getLiiquUserId(), 
	    					picUrl, 
	    					responseUpdater, 
	    					imageLoaderHandler);
	    		}
								
	    		Log.d(TAG, String.format("cached(%s) = %b ", picUrl, cached));
			}		
			
		} catch (ConnectException ce) {
			Log.d(TAG, "-", ce);
		} catch (IOException ioe) {
			Log.d(TAG, "-", ioe);
		} catch (ParseException pe) {
			Log.d(TAG, "-", pe);
		}
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
	}
}