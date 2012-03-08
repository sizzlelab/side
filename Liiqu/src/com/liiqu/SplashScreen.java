package com.liiqu;

import android.app.Activity;
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
import com.liiqu.util.IntentFactory;

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
		
		Log.d(TAG, "expiration " + facebook.getAccessExpires());
		
		if (facebook.getAccessExpires() > (System.currentTimeMillis() + TWO_HOURS)) {
			startNextActivity();
			return;
		}
		
		
		WebViewHelper.setup(webView, this, TAG, "splash.html");
	}
	
    private void startNextActivity() {
    	startActivity(IntentFactory.create(EventInformation2.class.getName()));
		finish();
	}

	public void onJSFacebookLogin() {
    	Log.d(TAG, "onJSFacebookLogin");

        facebook.authorize(this, permissions, 0, this);
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
    
    public void onComplete(Bundle values) {
        SessionStore.save(facebook, this);
        
        startNextActivity();
    }

    @Override
    public void onFacebookError(FacebookError error) {
    }

    @Override
    public void onError(DialogError error) {
    }

    @Override
    public void onCancel() {

    }
}
