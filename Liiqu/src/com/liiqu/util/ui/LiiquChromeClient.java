package com.liiqu.util.ui;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

public class LiiquChromeClient extends WebChromeClient {
	
	private String tag;

	public LiiquChromeClient(String tag) {
		
		this.tag = tag;
	}
	
	public boolean onConsoleMessage(ConsoleMessage cm) {
		Log.d(tag+"+Webview", 
				String.format("%s -- From line %d of %s", 
						cm.message(),
						cm.lineNumber(),
						cm.sourceId()));
	
	    return true;
	}
}
