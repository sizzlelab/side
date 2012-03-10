package com.liiqu;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpRequest;
import com.github.droidfu.http.BetterHttpResponse;
import com.liiqu.eventdetails.DetailedView;
import com.liiqu.facebook.SessionStore;
import com.liiqu.util.ui.WebViewHelper;

public class SplashScreen extends Activity implements DialogListener {

	private static final String TAG = SplashScreen.class.getSimpleName();
	
    public static final String APP_ID = "258571624190728";

	private static final long TWO_HOURS = 7200000;

	private static final String JS_SHOW_PROGRESS = "javascript:jsShowProgress()";

	private static final String JS_HIDE_PROGRESS = "javascript:jsHideProgress()";

    String[] mainItems = { };
    String[] permissions = { "offline_access", "publish_stream", "user_photos", "publish_checkins",
            "photo_upload" };

	
	private ViewGroup progressContainer;
	private WebView webView;

	private Facebook facebook;

	
	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.splash);

		progressContainer = (ViewGroup) findViewById(R.id.progress_container);
		webView = (WebView) findViewById(R.id.webview);
		
		facebook  = new Facebook(APP_ID);
		
		SessionStore.restore(facebook, this);
		
		final long expires = facebook.getAccessExpires();
		Log.d(TAG, "expiration " + expires);
		Log.d(TAG, "token " + facebook.getAccessToken());
		
		if ((expires == 0 && facebook.getAccessToken() != null) || 
			expires > (System.currentTimeMillis() + TWO_HOURS)) {
			startNextActivity();
			return;
		}
		
		WebViewHelper.setup(webView, this, TAG, "splash.html");
	}
	
    public void startNextActivity() {
    	startActivity(new Intent(this, DetailedView.class));
		finish();
	}

	public void onJSFacebookLogin() {
    	Log.d(TAG, "onJSFacebookLogin");

        facebook.authorize(this, permissions, 1, this);
    }
	
    public void onJSFinishedLoading() {
    	Log.d(TAG, "onJSFinishedLoading");
    	
    	runOnUiThread(new Runnable() {

			@Override
    		public void run() {
    			progressContainer.setVisibility(View.GONE);
    			webView.setVisibility(View.VISIBLE);
    		}
    	});
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {     	
        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    public void onComplete(Bundle values) {
		new IdentityDownloadTask(this, facebook).execute(facebook.getAccessToken());
    }

    @Override
    public void onFacebookError(FacebookError error) {
    	Log.d(TAG, "onFacebookError() " + error.getMessage());
    	
    	Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(DialogError error) {
    	Log.d(TAG, "onFacebookError() " + error.getMessage());
    }

    @Override
    public void onCancel() {
    	Log.d(TAG, "onCancel() ");
    }
    
    public void jsShowProgress() {
    	
    	webView.loadUrl(JS_SHOW_PROGRESS);
    }
    
    public void jsHideProgress() {
    	
    	webView.loadUrl(JS_HIDE_PROGRESS);
    }
}

class IdentityDownloadTask extends AsyncTask<String, Void, Integer> {

	private static final int SUCCESS = 201;
	
	private static final int FB_TOKEN_NOT_VALID = 400;
	
	private static final int ACCOUNT_DEACTIVATED = 403;
	
	private static final int USER_NOT_FOUND = 404;
	
	private static final int OTHER_ERROR = 410;

	private static final String TAG = IdentityDownloadTask.class.getSimpleName();
	
	private static final String LIIQU_SESSION_API = "https://staging.liiqu.com/api/session";

	private SplashScreen context;

	private Facebook facebook;

	public IdentityDownloadTask(SplashScreen context, Facebook facebook) {
		this.context = context;
		this.facebook = facebook;
	}
	
	@Override
	public void onPreExecute() {
		context.jsShowProgress();
	}
	
	
	@Override
    protected Integer doInBackground(String... tokens)  {
		try {		
			final String content = String.format("{\"fb_token\": \"%s\"}", tokens[0] + "xxxxx");
			final StringEntity entity = new StringEntity(content);
			
			Log.d(TAG, "Requesting: " + LIIQU_SESSION_API + " with " + content);
			
			final BetterHttpRequest request = BetterHttp.post(LIIQU_SESSION_API, entity);
			BetterHttpResponse response = request.send();

			final String userIdResponse = response.getResponseBodyAsString();
			Log.d(TAG, userIdResponse);
			
			final String csrfToken = response.getHeader("X-CSRF-Token");
			final String cookie = response.getHeader("Set-Cookie");

			final JSONObject userIdJSON = (JSONObject) new JSONParser().parse(userIdResponse);

			final SharedPreferences prefs = context.getSharedPreferences(
					LiiquPreferences.COMMON_FILE, 
					Activity.MODE_PRIVATE);
			final Editor edit = prefs.edit();

			Log.d(TAG, "json: " + userIdJSON);
			Log.d(TAG, "json: " + csrfToken);
			Log.d(TAG, "json: " + cookie);	
			
			edit.putString(LiiquPreferences.CSRF, csrfToken);
			edit.putString(LiiquPreferences.SESSION_ID, cookie);
			
			final long userId = (Long) ((JSONObject) userIdJSON.get("data")).get("user_id");
			edit.putLong(LiiquPreferences.USER_ID, userId);
			edit.commit();

			return response.getStatusCode();
			
		} catch (ConnectException ce) {
			
			context.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(context, R.string.failed_to_download_identity, Toast.LENGTH_LONG).show();
				}
			});
			
		} catch (UnsupportedEncodingException uee) {
			Log.d(TAG, "", uee);
		} catch (IOException ioe) {
			Log.d(TAG, "", ioe);
		} catch (ParseException e) {
			Log.d(TAG, "", e);
		}
		
    	return OTHER_ERROR ;
    }

    @Override
    protected void onPostExecute(Integer result) {
    	switch(result) {
    	case SUCCESS:
            SessionStore.save(facebook, context);
            context.startNextActivity();
    		
    		break;
    		
    	case ACCOUNT_DEACTIVATED:
    		context.jsHideProgress();
    		
    		Toast.makeText(context, R.string.account_deactivated, Toast.LENGTH_LONG).show();
    		break;
    		
    	case USER_NOT_FOUND:
    		context.jsHideProgress();
    		
    		Toast.makeText(context, R.string.account_not_found, Toast.LENGTH_LONG).show();
    		break;

    	case FB_TOKEN_NOT_VALID:
    	case OTHER_ERROR:
    		context.jsHideProgress();
    		
    		Toast.makeText(context, R.string.authentication_error, Toast.LENGTH_LONG).show();    		
    		break;
    	}
    	
    }
}