package com.liiqu.response;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import eu.mobileguild.db.MGDatabaseHelper;

public class ResponseDao {

	private static final String TAG = ResponseDao.class.getSimpleName();

    public static final String RESPONSE_CREATE = 
    	"create table response(" +
	    	"response_id integer primary key, " +
    		"liiqu_event_id NUMBER, " +
    		"liiqu_user_id NUMBER, " + 
    		"json TEXT, " +
	    	"UNIQUE(liiqu_event_id, liiqu_user_id) ON CONFLICT REPLACE " +
	    	")";
	
    public static final String RESPONSE_TABLE = "response";

    public static final String RESPONSE_DROP = "DROP TABLE IF EXISTS response";

	private MGDatabaseHelper dbHelper;
		
	public ResponseDao(MGDatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public long insert(Response response) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("liiqu_event_id", response.liiquEventId);
		values.put("liiqu_user_id", response.liiquUserId);
		values.put("json", response.json);
		
		synchronized(dbHelper) {
			return db.insert(RESPONSE_TABLE, "", values);
		}
	}

	public long replace(Response response) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues(	);
		
		values.put("liiqu_event_id", response.liiquEventId);
		values.put("liiqu_user_id", response.liiquUserId);
		values.put("json", response.json);
		
		synchronized(dbHelper) {
			return db.replace(RESPONSE_TABLE, "", values);
		}
	}	
	
	public Response getResponse(long liiquEventId, long liiquUserId) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "liiqu_event_id = ? AND liiqu_user_id = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(liiquEventId),
				Long.toString(liiquUserId),
		};
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		Log.d(TAG, builder.buildQuery(null, whereClause, whereArgs, null, null, null, null) );
		
		Cursor c = null;
		synchronized(dbHelper) {
			c = db.query(RESPONSE_TABLE, null, whereClause, whereArgs, null, null, null);
		}
		c.moveToFirst();
		
		if (c.getCount() == 0) {
			return null;
		}
		
		return cursorToResponse(c); 
	}
	
	public ArrayList<Response> getResponses(long liiquEventId) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "liiqu_event_id = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(liiquEventId),
		};
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		Log.d(TAG, builder.buildQuery(null, whereClause, whereArgs, null, null, null, null) );
		
		Cursor c = null;
		
		synchronized(dbHelper) {
			c = db.query(RESPONSE_TABLE, null, whereClause, whereArgs, null, null, null);
		}
		
		c.moveToFirst();
		
		if (c.getCount() == 0) {
			return null;
		}
		
		final ArrayList<Response> responses = new ArrayList<Response>();
		
		for (int i = 0; i<c.getCount(); i++) {
			c.moveToPosition(i);
			responses.add(cursorToResponse(c));
		}
		
		return responses; 
	}

	private Response cursorToResponse(final Cursor c) {
		final Response e = new Response();
		
		e.liiquEventId = c.getLong(c.getColumnIndexOrThrow("liiqu_event_id"));
		e.liiquUserId = c.getLong(c.getColumnIndexOrThrow("liiqu_user_id"));
		e.json = c.getString(c.getColumnIndexOrThrow("json"));
		
		return e;
	}
}
