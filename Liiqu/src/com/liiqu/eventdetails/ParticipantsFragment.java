package com.liiqu.eventdetails;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;


public class ParticipantsFragment extends EventDetailsFragment 
	{

	private boolean finishedEventInfoLoading = false;

	private boolean finishedResponsesLoading = false;

	public ParticipantsFragment() {
		super("participants_tab.html");
	}
	
	@Override
	public Loader<String> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader");
				
		DatabaseLoader loader = null;
		
		switch(id) {
		case DATABASE_LOADER_ID: 
			loader = new DatabaseLoader(
				getActivity(), 
				eventDao, responseDao,
				args.getLong(EventDetailsFragment.EVENT_ID)
				);
			break;
		
		case INTERNET_LOADER_ID: 
			loader = new InternetLoader(
				getActivity(),
				eventDao, responseDao,
				args.getLong(EventDetailsFragment.EVENT_ID),
				imageLoaderHandler
				);
			break;
		default: throw new RuntimeException("Shouldn't happen");
		}
		
		loader.setFilters(true, false);
		return loader;
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
	
	public void onJSChangeParticipation(String id, String name, String picture) {
		((DetailedView) activity).startRsvpActivity(id, name, picture);
	}

}

