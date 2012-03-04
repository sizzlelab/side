package com.liiqu.event;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.util.Log;

import com.liiqu.ImageDbUpdater;

public class EventImageUpdater implements ImageDbUpdater {

	public static String TAG = EventImageUpdater.class.getSimpleName();
	
	EventDao eventDao;
		
	public EventImageUpdater(EventDao eventDao) {
		this.eventDao = eventDao;
	}

	public void update(String id, String path) {

		try {
			final Event event = eventDao.getEvent(Long.parseLong(id));
			
			// CAUTION! JSONParser is not thread-safe
			final JSONObject json = (JSONObject) new JSONParser().parse(event.getJson());
			final JSONObject owner = (JSONObject) json.get("owner");
			final JSONObject picture = (JSONObject) owner.get("picture");
			
			picture.put("medium", path);
			
			event.setJson(json.toJSONString());

			eventDao.replace(event);
			
		} catch(NullPointerException npe) {
			Log.d(TAG, "npe ", npe);
		} catch (ParseException pe) {
			Log.d(TAG, "pe", pe);
		}
	}
}
