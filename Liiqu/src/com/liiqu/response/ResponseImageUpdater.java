package com.liiqu.response;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.util.Log;

import com.liiqu.util.ui.ImageDbUpdater;

public class ResponseImageUpdater implements ImageDbUpdater {

	public static String TAG = ResponseImageUpdater.class.getSimpleName();
	
	ResponseDao responseDao;
		
	public ResponseImageUpdater(ResponseDao responseDao) {
		this.responseDao = responseDao;
	}

	public void update(String liiquId, String path) {

		try {
			
			final int dividerIndex = liiquId.indexOf('_');
			final long liiquEventId = Long.parseLong(liiquId.substring(0, dividerIndex));
			final long liiquUserId = Long.parseLong(liiquId.substring(dividerIndex + 1));

			final Response response = responseDao.getResponse(liiquEventId, liiquUserId);
			
			// CAUTION! JSONParser is not thread-safe
			final JSONObject json = (JSONObject) new JSONParser().parse(response.getJson());
			final JSONObject owner = (JSONObject) json.get("user");
			final JSONObject picture = (JSONObject) owner.get("picture");
			
			picture.put("medium", path);
			
			response.setJson(json.toJSONString());

			responseDao.replace(response);
			
		} catch(NullPointerException npe) {
			Log.d(TAG, "npe ", npe);
		} catch (ParseException pe) {
			Log.d(TAG, "pe", pe);
		}
	}
}
