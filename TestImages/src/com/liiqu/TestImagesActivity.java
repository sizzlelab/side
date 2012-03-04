package com.liiqu;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

public class TestImagesActivity extends Activity {
    private static final String TAG = TestImagesActivity.class.toString();
	private WebView webView;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final String base = Environment.getExternalStorageDirectory().getAbsolutePath().toString(); 
        Log.d(TAG, "base : " + base);
        
        final String relative =  base + "" +"/DCIM/Camera/2010-03-27 15.40.09.jpg";
        
        String html = ("<html><head></head><body><img height='200px' width='200px' src=\"file://"+ relative + "\"></body></html>"); 
        
        
        Log.d(TAG, "base : " + html);

        File file = new File(relative);
        
        Log.d(TAG, "canRead" + file.exists());
        Log.d(TAG, "canRead" + file.canRead());
        
        
        webView = (WebView) findViewById(R.id.webview); 
        webView.getSettings().setAllowFileAccess(true); 
        webView.getSettings().setJavaScriptEnabled(true);  

        webView.loadDataWithBaseURL("file://", html, "text/html","utf-8", null);
        
    }
}