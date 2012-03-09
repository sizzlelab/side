package com.liiqu.eventdetails;

import com.liiqu.R;
import com.liiqu.SplashScreen;
import com.liiqu.R.drawable;
import com.liiqu.R.id;
import com.liiqu.R.layout;
import com.liiqu.R.string;
import com.liiqu.db.DatabaseHelper;
import com.liiqu.event.EventDao;
import com.liiqu.facebook.SessionStore;
import com.liiqu.response.ResponseDao;
import com.liiqu.util.ui.ImageHtmlLoaderHandler;
import com.liiqu.util.ui.WebViewHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.SupportActivity;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public abstract class EventDetailsFragment 
	extends Fragment 
	implements LoaderManager.LoaderCallbacks<String>
	{
	
	private static final String JAVASCRIPT_UPDATE_DATA = "javascript:jsUpdateData();";

	private static final String JAVASCRIPT_CHANGE_PARTICIPATION = "javascript:window.changeParticipation(\"%s\", \"%s\")";

	
	public static final String EVENT_ID = "event id";

	
	public final String TAG = getClass().getSimpleName();

	protected DatabaseHelper dbHelper;

	protected EventDao eventDao;

	protected ResponseDao responseDao;

	protected SupportActivity activity;

	private ViewGroup progressContainer;

	protected WebView webView;

	protected ImageHtmlLoaderHandler imageLoaderHandler;

	protected String loaderResult;

	protected String htmlPage;

	protected static final int DATABASE_LOADER_ID = 1;
	protected static final int INTERNET_LOADER_ID = 2;

	public EventDetailsFragment(String page) {
		
		this.htmlPage = page; 
	}
	
	@Override
	public void onAttach(SupportActivity context) {
		super.onAttach(context);
		
		dbHelper = new DatabaseHelper(context.getBaseContext());
		
		eventDao = new EventDao(dbHelper);
		responseDao = new ResponseDao(dbHelper);
		
		activity = context;
		
		Log.d(TAG, "TAG: " + R.id.pager);
		Log.d(TAG, "TAG: " + this.getTag());
	}
	
	@Override
	public void onActivityCreated (Bundle sis) {
		super.onActivityCreated(sis);
		Log.d(TAG, "onActivityCreated");
		
		setHasOptionsMenu(true);
		
		getLoaderManager().initLoader(DATABASE_LOADER_ID, getArguments(), this);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateView");
		
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.event_information, null);
		
		progressContainer = (ViewGroup) root.findViewById(R.id.progress_container);
		webView = (WebView) root.findViewById(R.id.webview);
	
	    imageLoaderHandler = new ImageHtmlLoaderHandler(webView);
		
		WebViewHelper.setup(webView, (Context) activity, this, TAG, htmlPage);
	    
		return root;
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "onCreateOptionsMenu");
		final MenuItem refreshMenuItem = menu.add(
				R.id.common_menu, 
				R.id.refresh_menu, 
				Menu.CATEGORY_SYSTEM, 
				R.string.refresh_menu);
		
		refreshMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		refreshMenuItem.setIcon(R.drawable.refresh);		

		final MenuItem logoutMenuItem = menu.add(
				R.id.common_menu, 
				R.id.logout_menu, 
				Menu.CATEGORY_SYSTEM, 
				R.string.logout_menu);
		
		logoutMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		logoutMenuItem.setIcon(R.drawable.logout);		
	}
	
	@Override 
    public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		
		getLoaderManager().destroyLoader(DATABASE_LOADER_ID);
		getLoaderManager().destroyLoader(INTERNET_LOADER_ID);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.refresh_menu:
			onRefresh();
			return true;
		
		case R.id.logout_menu:
			onLogout();
			
			return true;
		}
		
		return false;
	}

	
	private void onLogout() {
		SessionStore.clear((Context) activity);
		
		startActivity(new Intent((Context) activity, SplashScreen.class));
	}

	private void onRefresh() {
		progressContainer.setVisibility(View.VISIBLE);
		webView.setVisibility(View.GONE);
		
		getLoaderManager().restartLoader(INTERNET_LOADER_ID, getArguments(), this);
	}
	
	@Override
	public void onLoadFinished(Loader<String> loader, String data) {
		Log.d(TAG, "onLoadFinished");
		
		loaderResult = data;
		
		webView.loadUrl(String.format(JAVASCRIPT_UPDATE_DATA));
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {
		Log.d(TAG, "onLoaderReset");
		
		loaderResult = null;
	}

	protected void onFinishedLoadingData() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				progressContainer.setVisibility(View.GONE);
				webView.setVisibility(View.VISIBLE);
			}
		});
	}
	
	public void onChangeParticipation(String userId, String choice) {

		webView.loadUrl(String.format(JAVASCRIPT_CHANGE_PARTICIPATION, userId, choice));
	}
	

	public String getJSData() {
		Log.d(TAG, "getJSData()");
		Log.d(TAG, "result:" + loaderResult);
		
		return loaderResult;
	}
}
