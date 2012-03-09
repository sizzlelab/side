package com.liiqu.eventdetails;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.liiqu.event.Event;
import com.liiqu.event.EventDao;
import com.liiqu.response.Response;
import com.liiqu.response.ResponseDao;

class DatabaseLoader extends AsyncTaskLoader<String> {
	
	public static final String TAG = DatabaseLoader.class.getSimpleName(); 
	
	protected EventDao eventDao;
	protected ResponseDao responseDao;
	protected long liiquEventId;

	protected boolean filterOutEvent;
	protected boolean filterOutResponses;
	
	public DatabaseLoader(Context context, EventDao eventDao, ResponseDao responseDao, long liiquEventId) {
		super(context);
		
		this.liiquEventId = liiquEventId;
		
        this.eventDao = eventDao;
        this.responseDao = responseDao;
	}
           
    public void setFilters(boolean filterOutEvent, boolean filterOutResponses) {
        this.filterOutEvent = filterOutEvent;
        this.filterOutResponses = filterOutResponses;
	}

	@Override
	public String loadInBackground() {
		Log.d(TAG, "loadInBackground " + liiquEventId);
		
		final Event event = filterOutEvent 
				? null
				: eventDao.getEvent(liiquEventId);
		
		final ArrayList<Response> responses = filterOutResponses 
				? null
				: responseDao.getResponses(liiquEventId);

		final StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		
		if (event != null) {
			buffer.append("\"event\":");
			buffer.append(event.getJson());
			buffer.append(",");
		} 
		
		buffer.append("\"responses\": [");
		
		if (responses != null) {
			for (Response response: responses) {
				buffer.append(response.getJson());
				buffer.append(",");
			}
			buffer.setLength(buffer.length() -1);
		}
		
		buffer.append("]}");		
		
		Log.d(TAG, buffer.toString());
		
		return buffer.toString();
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
	}

}