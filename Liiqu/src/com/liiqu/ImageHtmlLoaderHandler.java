package com.liiqu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.github.droidfu.imageloader.ImageLoader;

public class ImageHtmlLoaderHandler extends Handler {

	public final static String JAVASCRIPT_CODE = "javascript:changePicture(\"%s\",\"%s\")";
	
	private WebView webView;

	public final String TAG = getClass().getSimpleName(); 
	
	
	public ImageHtmlLoaderHandler(WebView webView) {
		this.webView = webView;
	}

	public WebView getWebView() {
		return webView;
	}

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == ImageLoader.HANDLER_MESSAGE_ID) {
            handleImageLoadedMessage(msg);
        }
    }

    protected final void handleImageLoadedMessage(Message msg) {
        Bundle data = msg.getData();
        final String path = data.getString(ImageHtmlLoader.IMAGE_FILE_EXTRA);
        final String imageId = data.getString(ImageHtmlLoader.IMAGE_ID_EXTRA);
        
        handleImageLoaded(imageId, path, msg);
    }

    protected boolean handleImageLoaded(String imageId, String path, Message msg) {

    	Log.d(TAG, String.format("handleImageLoaded(%s, %s)", imageId, path));
    	webView.loadUrl(String.format(JAVASCRIPT_CODE, imageId, path));
    	
        return true;
    }
}
