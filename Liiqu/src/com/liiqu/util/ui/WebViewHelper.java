package com.liiqu.util.ui;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import com.liiqu.util.AssetUtil;

public class WebViewHelper {

	
	public static void setup(final WebView webView, Context context, Object obj, String tag, String filename) {

        webView.getSettings().setAllowFileAccess(true); 
        webView.getSettings().setJavaScriptEnabled(true);  

        webView.setWebChromeClient(new LiiquChromeClient(tag));
        
        webView.addJavascriptInterface(obj, "Android");
        
        final String html = AssetUtil.readAssetsFile(context, filename);
        
        webView.loadDataWithBaseURL("file://", html, "text/html","utf-8", null);
	}
	
	public static void setup(final WebView webView, Context context, String tag, String filename) {
		setup(webView, context, context, tag, filename);
	}
}
