package com.liiqu.event;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import eu.mobileguild.db.MGDatabaseHelper;



public class EventDao {

	private static final String TAG = EventDao.class.getSimpleName();

    public static final String EVENT_CREATE = 
    	"create table event (" +
	    	"event_id integer primary key, " +
    		"liiqu_event_id NUMBER, " +
	    	"json TEXT, " +
	    	"UNIQUE(liiqu_event_id) ON CONFLICT REPLACE " +
	    	")";
	
    public static final String EVENT_TABLE = "event";

    public static final String EVENT_DROP = "DROP TABLE IF EXISTS event";

	private MGDatabaseHelper dbHelper;
		
	public EventDao(MGDatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public long insert(Event event) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("liiqu_event_id", event.liiquEventId);
		values.put("json", event.json);

		synchronized(dbHelper) {
			return db.insert(EVENT_TABLE, "", values);
		}
	}

	public long replace(Event event) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues(	);
		
		values.put("liiqu_event_id", event.liiquEventId);
		values.put("json", event.json);

		synchronized(dbHelper) {
			return db.replace(EVENT_TABLE, "", values);
		}
	}	
	
	public Event getEvent(Long liiquEventId) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "liiqu_event_id = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(liiquEventId)
		};
				
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		Log.d(TAG, builder.buildQuery(null, whereClause, whereArgs, null, null, null, null) );
		
		synchronized(dbHelper) {
			final Cursor c = db.query(EVENT_TABLE, null, whereClause, whereArgs, null, null, null);
			
			c.moveToFirst();
			
			if (c.getCount() == 0) {
				return null;
			}
			
			final Event event = cursorToEvent(c);
			c.close();
			return event;
		}
	}

	private Event cursorToEvent(final Cursor c) {
		final Event e = new Event();
		
		e.liiquEventId = c.getLong(c.getColumnIndexOrThrow("liiqu_event_id"));
		e.json = c.getString(c.getColumnIndexOrThrow("json"));
		
		return e;
	}
}
