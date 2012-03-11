package com.liiqu;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.support.v4.app.SupportActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.facebook.android.R;
import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpRequest;
import com.github.droidfu.http.BetterHttpResponse;
import com.liiqu.facebook.SessionStore;

public class IdentityDownloadTask extends AsyncTask<Facebook, Void, Integer> {

	/* Documentation: http://dev.liiqu.com/api/session.html */
	private static final String LIIQU_SESSION_API = LiiquPreferences.ROOT_URL + "api/session";
	
	public static final int SUCCESS = 201;
	
	private static final int FB_TOKEN_NOT_VALID = 400;
	
	private static final int ACCOUNT_DEACTIVATED = 403;
	
	private static final int USER_NOT_FOUND = 404;
	
	private static final int OTHER_ERROR = 410;
	
	public static final int CONNECTION_ERROR = -2;

	private static final String TAG = IdentityDownloadTask.class.getSimpleName();
	

	private Activity activity;

	private LiiquLoginListener listener;

	private SupportActivity supportActivity;

	public IdentityDownloadTask(Activity activity) {
		this.activity = activity;
	}

	public IdentityDownloadTask(SupportActivity supportActivity) {
		this.supportActivity = supportActivity;
	}

	
	
	public void setLiiquLoginListener(LiiquLoginListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void onPreExecute() {
		
	}
	
	
	@Override
    public Integer doInBackground(Facebook... facebooks)  {
		try {		
			
			final Facebook facebook = facebooks[0];
			
			final String content = String.format("{\"fb_token\": \"%s\"}", facebook.getAccessToken());
			final StringEntity entity = new StringEntity(content);
			
			Log.d(TAG, "Requesting: " + LIIQU_SESSION_API + " with " + content);
			
			final BetterHttpRequest request = BetterHttp.post(LIIQU_SESSION_API, entity);
			BetterHttpResponse response = request.send();

			final String userIdResponse = response.getResponseBodyAsString();
			Log.d(TAG, userIdResponse);
			
			final String csrfToken = response.getHeader("X-CSRF-Token");
			final String cookie = response.getHeader("Set-Cookie");

			final JSONObject userIdJSON = (JSONObject) new JSONParser().parse(userIdResponse);

			final SharedPreferences prefs = activity.getSharedPreferences(
					LiiquPreferences.COMMON_FILE, 
					Activity.MODE_PRIVATE);
			final Editor edit = prefs.edit();

			Log.d(TAG, "json: " + userIdJSON);
			Log.d(TAG, "json: " + csrfToken);
			Log.d(TAG, "json: " + cookie);	
			
			edit.putString(LiiquPreferences.CSRF, csrfToken);
			edit.putString(LiiquPreferences.SESSION_ID, cookie);
			
			BetterHttp.setDefaultHeader("X-CSRF-Token", csrfToken);
			BetterHttp.setDefaultHeader("Set-Cookie", cookie);

			
			final long userId = (Long) ((JSONObject) userIdJSON.get("data")).get("user_id");
			edit.putLong(LiiquPreferences.USER_ID, userId);
			edit.commit();

			return response.getStatusCode();			
		} catch (ConnectException ce) {
			
			return CONNECTION_ERROR;
			
		} catch (UnsupportedEncodingException uee) {
			Log.d(TAG, "", uee);
		} catch (IOException ioe) {
			Log.d(TAG, "", ioe);
		} catch (ParseException e) {
			Log.d(TAG, "", e);
		}
		
    	return OTHER_ERROR ;
    }

	private void showToast(final int messageId) {
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(activity, messageId, Toast.LENGTH_LONG).show();
				}
			});
		} else {
			supportActivity.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(supportActivity.getBaseContext(), messageId, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

    @Override
	public void onPostExecute(Integer result) {
    	int msg = 0;
    	
    	switch(result) {
    	case SUCCESS:
    		if (listener == null) {
    			return;
    		}
			listener.onLoginSuccess();
    		
    		return;
    	
    	case ACCOUNT_DEACTIVATED:
    		msg = R.string.account_deactivated;
    		break;
    		
    	case USER_NOT_FOUND:    
    		msg = R.string.account_not_found;
    		break;

    	case FB_TOKEN_NOT_VALID:
    	case OTHER_ERROR:
    		msg = R.string.authentication_error;    		
    		break;
    		
    	case CONNECTION_ERROR:
        	if (listener != null) {
        		listener.onConnectionError();
        	}
 
			return;
    	}
    	
    	showToast(msg);
    	
    	if (listener != null) {
    		listener.onLoginFailure();
    	}
    }
}