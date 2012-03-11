package com.liiqu.eventdetails;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpRequest;
import com.github.droidfu.http.BetterHttpResponse;
import com.liiqu.IdentityDownloadTask;
import com.liiqu.LiiquPreferences;
import com.liiqu.R;
import com.liiqu.facebook.SessionStore;
import com.liiqu.response.Response;
import com.liiqu.response.ResponseDao;

class ParticipationUpdateTask extends AsyncTask<String, Void, Integer> {

	private static final String TAG = ParticipationUpdateTask.class.getSimpleName();

	/* Documentation:  http://dev.liiqu.com/api/events/event_id/responses/response_id.html */
	private static final String LIIQU_RESPONSES_API = LiiquPreferences.ROOT_URL + "api/events/%d/responses/%d";

	private static final int SUCCESS = 200;
	private static final int OTHER_ERROR = 400;
	private static final int NOT_AUTHENTICATED = 401;
	private static final int NOT_ALLOWED_TO_RESPOND = 403;

	private static final int NOOP = -100;

	private static final int CONNECTION_ERROR = -1;
	
	private ChooseParticipation context;

	private ResponseDao responseDao;
	
	IdentityDownloadTask identityTask; 
	

	public ParticipationUpdateTask(ChooseParticipation context, ResponseDao responseDao) {
		this.context = context;
		this.responseDao = responseDao;
		
		identityTask = new IdentityDownloadTask(context);
	}
	
	@Override
	public void onPreExecute() {
		context.showProgress(true);
		
		identityTask.onPreExecute();
	}
	
	
	@Override
    protected Integer doInBackground(String... params)  {
		try {		
			final Facebook fbSession = new Facebook(LiiquPreferences.FACEBOOK_APP_ID);
			SessionStore.restore(fbSession, context);
			
			final Integer identityResponseCode = identityTask.doInBackground(fbSession);
			if (identityResponseCode == IdentityDownloadTask.CONNECTION_ERROR) {
				return CONNECTION_ERROR;
			} else
			if (identityResponseCode != IdentityDownloadTask.SUCCESS) {
				
				identityTask.onPostExecute(identityResponseCode);
				return NOOP;
			}
			identityTask.onPostExecute(identityResponseCode);
			
			
			final long eventId = Long.parseLong(params[0]);
			final long userId = Long.parseLong(params[1]);
			final String status = params[2];
			
			final String content = String.format("{\"status\": \"%s\"}", status);
			final StringEntity entity = new StringEntity(content);
			
			final String url = String.format(LIIQU_RESPONSES_API, eventId, userId);
			
			Log.d(TAG, "Requesting: " + url + " with " + content);
			
			final BetterHttpRequest request = BetterHttp.put(url, entity);
			BetterHttpResponse httpResponse = request.send();

			final int statusCode = httpResponse.getStatusCode();
			Log.d(TAG, "statusCode: " + statusCode);
			
			if (statusCode != SUCCESS) {
				return statusCode;
			}
			
			Response response = responseDao.getResponse(eventId, userId);
			final JSONObject root = (JSONObject) new JSONParser().parse(response.getJson());
			
			root.put("status", status);
			
			response.setJson(root.toJSONString());			
			responseDao.replace(response);
			
			return statusCode;
			
		} catch (ConnectException ce) {
			
			
			return CONNECTION_ERROR;
		} catch (UnsupportedEncodingException uee) {
			Log.d(TAG, "", uee);
		} catch (ParseException e) {
			Log.d(TAG, "", e);
		}
		
    	return OTHER_ERROR ;
    }

    @Override
    protected void onPostExecute(Integer result) {
    	switch(result) {
    	case SUCCESS:
            context.returnChoice();
    		break;
    	
    	case NOT_AUTHENTICATED:
    	case NOT_ALLOWED_TO_RESPOND:
    	case OTHER_ERROR:
    		
    		showToast(R.string.account_deactivated);
    		context.showProgress(false);
    		break;
    	
    	case CONNECTION_ERROR:
			showToast(R.string.connection_problem);
			return;
    	
    	case NOOP: 
    		context.showProgress(false);
    		
    		return;
    		
    	default: throw new RuntimeException("Shouldn't happen!");
    	}
    }

	private void showToast(final int msgId) {
		context.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(context, msgId, Toast.LENGTH_LONG).show();
			}
		});
	}
}