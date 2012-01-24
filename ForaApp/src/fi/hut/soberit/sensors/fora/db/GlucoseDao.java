package fi.hut.soberit.sensors.fora.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import eu.mobileguild.db.MGDatabaseHelper;
import fi.hut.soberit.sensors.utils.PeriodType;

public class GlucoseDao {

	private static final String TAG = GlucoseDao.class.getSimpleName();

	MGDatabaseHelper dbHelper;

    public static final String GLUCOSE_CREATE = 
    	"create table glucose (" +
	    	"glucose_id integer primary key, " +
	    	"glucose INTEGER, " +
	    	"type INTEGER, " +
	    	"time DATETIME, " + 
	    	"comment TEXT, " +
	    	"UNIQUE(time, glucose, type) ON CONFLICT REPLACE " +
	    	")";
	
    public static final String GLUCOSE_TABLE = "glucose";

    public static final String GLUCOSE_DROP = 
    	"DROP TABLE IF EXISTS glucose_table";
	
	
	public GlucoseDao(MGDatabaseHelper dbHelper) {
		super();
		this.dbHelper = dbHelper;
	}
	
	public long insert(Glucose glucose) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("glucose", glucose.getGlucose());
		values.put("type", glucose.getType());
		values.put("time", glucose.getTime());
		
		return db.insert(GLUCOSE_TABLE, "", values);
	}

	public long replace(Glucose glucose) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("glucose", glucose.getGlucose());
		values.put("type", glucose.getType());
		values.put("time", glucose.getTime());
		
		return db.replace(GLUCOSE_TABLE, "", values);
	}

	
	public int update(Glucose glucose) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();

		final ContentValues values = new ContentValues();
		
		values.put("value", glucose.getGlucose());
		values.put("type", glucose.getType());
		values.put("time", glucose.getTime());
		
		final String whereClause = "glucose_id = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(glucose.getId())
		};
		
		return db.update(GLUCOSE_TABLE, values, whereClause, whereArgs);
	}
	
	public ArrayList<Glucose> getMeasurements(Date start, Date end) {
		return getMeasurements(start.getTime(), end.getTime());
	}
	
	public ArrayList<Glucose> getMeasurements(long start, long end) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "time >= ? AND time <= ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(start),
				Long.toString(end)
		};

		final String orderBy = "time ASC";
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		Log.d(TAG, builder.buildQuery(null, whereClause, whereArgs, null, null, null, null) );
		
		final Cursor c = db.query(GLUCOSE_TABLE, null, whereClause, whereArgs, null, null, orderBy);
		
		c.moveToFirst();
		
		final ArrayList<Glucose> list = new ArrayList<Glucose>();
		for(int i =0; i<c.getCount(); i++) {
			c.moveToPosition(i);
			
			list.add(cursorToGlucose(c));
		}
		
		return list; 
	}

	private Glucose cursorToGlucose(final Cursor c) {
		final Glucose glucose = new Glucose(
				c.getLong(c.getColumnIndexOrThrow("time")),
				c.getInt(c.getColumnIndexOrThrow("glucose")),
				c.getInt(c.getColumnIndexOrThrow("type"))
		);
		
		glucose.setId(c.getLong(c.getColumnIndexOrThrow("glucose_id")));
		return glucose;
	}

	final static Calendar calendar = Calendar.getInstance();
	
	public ArrayList<Glucose> getMeasurements(Date day) {
		calendar.setTime(day);
		
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		long start = calendar.getTimeInMillis();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MILLISECOND, -1);
		
		long end = calendar.getTimeInMillis();
		
		return getMeasurements(start, end);
	}

	public Glucose find(long time, int glucose, int type) {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String whereClause = "time = ? AND glucose = ? AND type = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(time),
				Integer.toString(glucose),
				Integer.toString(type)
		};
		
		final Cursor c = db.query(GLUCOSE_TABLE, null, whereClause, whereArgs, null, null, null);
		
		if (c.getCount() == 0) {
			return null;
		}
		
		c.moveToFirst();
		return cursorToGlucose(c);
	}

	public ArrayList<Glucose> getMeasurements() {
		return getMeasurements(0l, Long.MAX_VALUE);
	}
}
