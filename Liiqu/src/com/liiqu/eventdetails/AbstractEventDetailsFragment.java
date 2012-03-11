package com.liiqu.eventdetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.liiqu.LiiquPreferences;
import com.liiqu.R;
import com.liiqu.SplashScreen;
import com.liiqu.db.DatabaseHelper;
import com.liiqu.event.EventDao;
import com.liiqu.facebook.SessionStore;
import com.liiqu.response.ResponseDao;
import com.liiqu.util.ui.ImageHtmlLoaderHandler;
import com.liiqu.util.ui.WebViewHelper;

public abstract class AbstractEventDetailsFragment 
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

	public AbstractEventDetailsFragment(String page) {
		
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
		
		dbHelper.close();
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
	public Loader<String> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader " + id);

		switch(id) {
		case DATABASE_LOADER_ID: 
		{
			final DatabaseLoader loader = new DatabaseLoader(
				getActivity(), 
				eventDao, responseDao,
				args.getLong(AbstractEventDetailsFragment.EVENT_ID)
				);
			loader.setFilters(true, false);
			
			return loader;
		}
					
		
		case INTERNET_LOADER_ID: 
			final InternetLoader loader = new InternetLoader(
				getActivity(),
				eventDao, responseDao,
				args.getLong(AbstractEventDetailsFragment.EVENT_ID),
				imageLoaderHandler
				);
			return loader;
			
		default: throw new RuntimeException("Shouldn't happen");
		}
		
	}
	
	@Override
	public void onLoadFinished(Loader<String> loader, String data) {
		Log.d(TAG, "onLoadFinished " + loader.getId());
		
		if (loader.getId() == DATABASE_LOADER_ID) {
			loaderResult = data;
			webView.loadUrl(String.format(JAVASCRIPT_UPDATE_DATA));
			return;
		}
		
		((EventDetailsActivity) activity).onEventDetailsRenewed();
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {
		Log.d(TAG, "onLoaderReset");
		
		loaderResult = null;
	}

	protected void onFinishedLoadingData() {
		Log.d(TAG, "onFinishedLoadingData");
		
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
	
	public String getJSUserId() {
		final SharedPreferences prefs = activity.getSharedPreferences(
				LiiquPreferences.COMMON_FILE, Activity.MODE_PRIVATE);
	
		long userId = prefs.getLong(LiiquPreferences.USER_ID, -1);
		
		if (userId == -1) {
			throw new RuntimeException("Shouldn't happen");
		}
		
		return Long.toString(userId);
	}
	
	public void setJSChangeParticipation(String elementId, String userId, String name, String picture) {
		((EventDetailsActivity) activity).startRsvpActivity(elementId, Long.parseLong(userId), name, picture);
	}

	public void onRefreshFromDatabase() {
		getLoaderManager().restartLoader(DATABASE_LOADER_ID, getArguments(), this);
	}

}
