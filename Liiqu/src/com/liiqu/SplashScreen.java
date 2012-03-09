package com.liiqu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.liiqu.facebook.SessionStore;
import com.liiqu.util.ui.WebViewHelper;

public class SplashScreen extends Activity implements DialogListener {

	private static final String TAG = SplashScreen.class.getSimpleName();
	
    public static final String APP_ID = "258571624190728";

	private static final long TWO_HOURS = 7200000;

    String[] mainItems = {  };
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
	
    private void startNextActivity() {
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
        SessionStore.save(facebook, this);
        
		Log.d(TAG, "expiration " + facebook.getAccessExpires());
		Log.d(TAG, "token " + facebook.getAccessToken());
        
        startNextActivity();
    }

    @Override
    public void onFacebookError(FacebookError error) {
    	Log.d(TAG, "onFacebookError() " + error.getMessage());
    }

    @Override
    public void onError(DialogError error) {
    	Log.d(TAG, "onFacebookError() " + error.getMessage());
    }

    @Override
    public void onCancel() {
    	Log.d(TAG, "onCancel() ");
    }
}
