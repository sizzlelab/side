package com.liiqu.eventdetails;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;


public class ParticipantsFragment extends AbstractEventDetailsFragment 
	{

	private boolean finishedEventInfoLoading = false;

	private boolean finishedResponsesLoading = false;

	public ParticipantsFragment() {
		super("participants_tab.html");
	}
	
		
	public void onJSFinishedEventInfoLoading() {
		finishedEventInfoLoading = true;
		
		Log.d(TAG, "onJSFinishedEventInfoLoading = " + (!finishedEventInfoLoading || !finishedResponsesLoading));
		
		if (!finishedEventInfoLoading || !finishedResponsesLoading) { 
			return;
		}

		onFinishedLoadingData();
		
		finishedEventInfoLoading = false;
		finishedResponsesLoading = false;
	}
	
	public void onJSFinishedResponsesLoading() {
		finishedResponsesLoading = true;

		Log.d(TAG, "onJSFinishedResponsesLoading = " + (!finishedEventInfoLoading || !finishedResponsesLoading));
		
		if (!finishedEventInfoLoading || !finishedResponsesLoading) { 
			return;
		}

		onFinishedLoadingData();
		
		finishedEventInfoLoading = false;
		finishedResponsesLoading = false;
	}
	
}

