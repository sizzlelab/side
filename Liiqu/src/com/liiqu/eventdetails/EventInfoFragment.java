package com.liiqu.eventdetails;



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
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.liiqu.db.DatabaseHelper;
import com.liiqu.facebook.SessionStore;
import com.liiqu.util.AssetUtil;


public class EventInfoFragment extends EventDetailsFragment 
	{

	private boolean finishedEventInfoLoading = false;

	private boolean finishedResponsesLoading = false;

	public EventInfoFragment() {
		super("event_info.html");
	}
	
	@Override
	public Loader<String> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");
				
		switch(id) {
		case DATABASE_LOADER_ID: return new DatabaseLoader(
				getActivity(), 
				eventDao, responseDao,
				args.getLong(EventDetailsFragment.EVENT_ID)
				);
		
		case INTERNET_LOADER_ID: return new InternetLoader(
				getActivity(),
				eventDao, responseDao,
				args.getLong(EventDetailsFragment.EVENT_ID),
				imageLoaderHandler
				);
		}
		
		throw new RuntimeException("Shouldn't happen");
	}
	
	public void onJSFinishedEventInfoLoading() {
		finishedEventInfoLoading = true;
		
		Log.d(TAG, "onJSFinishedEventInfoLoading");
		
		if (!finishedEventInfoLoading || !finishedResponsesLoading) { 
			return;
		}

		onFinishedLoadingData();
		
		finishedEventInfoLoading = false;
		finishedResponsesLoading = false;
	}
	
	public void onJSFinishedResponsesLoading() {
		finishedResponsesLoading = true;

		Log.d(TAG, "onJSFinishedResponsesLoading");
		
		if (!finishedEventInfoLoading || !finishedResponsesLoading) { 
			return;
		}

		onFinishedLoadingData();
		
		finishedEventInfoLoading = false;
		finishedResponsesLoading = false;
	}

	
	public String getJSParticipation() {
		return "maybe";
	}
	
	public void jsOpenMap(String uri) {
		((DetailedView) activity).openMap(uri);
	}
	
	public void onJSChangeMyParticipation() {
		((DetailedView) activity).startRsvpActivity(
				"right-header",
				"Maksim Golivkin",
				"https://graph.facebook.com/1540570866/picture?type=square"
				);
	}
}