package fi.hut.soberit.sensors.fora.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import eu.mobileguild.db.MGDatabaseHelper;
import fi.hut.soberit.sensors.utils.PeriodType;

public class PulseDao {

	private static final String TAG = PulseDao.class.getSimpleName();
	
	MGDatabaseHelper dbHelper;

    public static final String PULSE_CREATE = 
    	"create table pulse (" +
	    	"pulse_id integer primary key, " +
	    	"time DATETIME, " +
	    	"pulse INTEGER, " +
	    	"UNIQUE(time, pulse) ON CONFLICT REPLACE " +
	    	")";

    public static final String PULSE_DROP = 
    	"DROP TABLE IF EXISTS pulse";

    public static final String PULSE_TABLE = "pulse";
    
	
	public PulseDao(MGDatabaseHelper dbHelper) {
		super();
		this.dbHelper = dbHelper;
	}
	
	public long insert(Pulse record) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();

		values.put("time", record.getTime());
		values.put("pulse", record.getPulse());
		
		return db.insert(PULSE_TABLE, "", values);
	}

	public long replace(Pulse record) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("pulse", record.getPulse());
		values.put("time", record.getTime());
		
		return db.replace(PULSE_TABLE, "", values);
	}

	
	public int update(Pulse record) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();

		final ContentValues values = new ContentValues();
		
		values.put("pulse", record.getPulse());
		values.put("time", record.getTime());
		
		final String whereClause = "pulse_id = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(record.getId())
		};
		
		return db.update(PULSE_TABLE, values, whereClause, whereArgs);
	}
	
	public ArrayList<Pulse> getMeasurements(Date start, Date end) {
		return getMeasurements(start.getTime(), end.getTime());
	}
	
	public ArrayList<Pulse> getMeasurements(long start, long end) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "time >= ? AND time <= ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(start),
				Long.toString(end)
		};
		
		final String orderBy = "time ASC";
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		Log.d(TAG, builder.buildQuery(null, whereClause, whereArgs, null, null, null, null) );
		
		final Cursor c = db.query(PULSE_TABLE, null, whereClause, whereArgs, null, null, orderBy);
		
		c.moveToFirst();
		
		final ArrayList<Pulse> list = new ArrayList<Pulse>();
		for(int i =0; i<c.getCount(); i++) {
			c.moveToPosition(i);
			final Pulse glucose = new Pulse(
					c.getLong(c.getColumnIndexOrThrow("time")),
					c.getInt(c.getColumnIndexOrThrow("pulse"))
			);
			
			glucose.setId(c.getLong(c.getColumnIndexOrThrow("pulse_id")));
			
			list.add(glucose);
		}
		
		return list; 
	}
	
	public ArrayList<Pulse> getMeasurements() {
		return getMeasurements(0l, Long.MAX_VALUE);
	}
	
	final static Calendar calendar = Calendar.getInstance();
	
	public ArrayList<Pulse> getMeasurements(Date day) {
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
}
