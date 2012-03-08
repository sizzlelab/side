package com.liiqu;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class SplashScreen extends Activity {

	private static final String TAG = SplashScreen.class.getSimpleName();
	
	private ViewGroup progressContainer;
	private WebView webView;

	
	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.splash);

		progressContainer = (ViewGroup) findViewById(R.id.progress_container);
		webView = (WebView) findViewById(R.id.webview);
		
		WebViewHelper.setup(webView, this, TAG, "splash.html");
	}
	
    public void onJSFacebookLogin() {
    	Log.d(TAG, "onJSFacebookLogin");

    	
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
}
