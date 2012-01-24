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

public class TemperatureDao {

	private static final String TAG = TemperatureDao.class.getSimpleName();

	MGDatabaseHelper dbHelper;

    public static final String TEMPERATURE_CREATE = 
    	"create table temperature (" +
	    	"temperature_id integer primary key, " +
	    	"temperature FLOAT, " +
	    	"time DATETIME," +
	    	"UNIQUE(time, temperature) ON CONFLICT REPLACE " +
	    	")";
	
    public static final String TEMPERATURE_TABLE = "temperature";

    public static final String TEMPERATURE_DROP = "DROP TABLE IF EXISTS temperature";
	
    protected String table = TEMPERATURE_TABLE;
    
	public TemperatureDao(MGDatabaseHelper dbHelper) {
		super();
		this.dbHelper = dbHelper;
	}
	
	public long insert(Temperature temperature) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("temperature", temperature.getTemperature());
		values.put("time", temperature.getTime());
		
		return db.insert(table, "", values);
	}

	public long replace(Temperature temperature) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("temperature", temperature.getTemperature());
		
		return db.replace(table, "", values);
	}

	
	public int update(Temperature temperature) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();

		final ContentValues values = new ContentValues();
		
		values.put("temperature", temperature.getTemperature());
		values.put("time", temperature.getTime());
		
		final String whereClause = "temperature_id = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(temperature.getId())
		};
		
		return db.update(table, values, whereClause, whereArgs);
	}
	
	public ArrayList<Temperature> getMeasurements(Date start, Date end) {
		return getMeasurements(start.getTime(), end.getTime());
	}
	
	public ArrayList<Temperature> getMeasurements() {
		return getMeasurements(0l, Long.MAX_VALUE);
	}
	
	public ArrayList<Temperature> getMeasurements(long start, long end) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "time >= ? AND time <= ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(start),
				Long.toString(end)
		};
		
		final String orderBy = "time ASC";
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		Log.d(TAG, builder.buildQuery(null, whereClause, whereArgs, null, null, null, null) );
		
		final Cursor c = db.query(table, null, whereClause, whereArgs, null, null, orderBy);
		
		c.moveToFirst();
		
		final ArrayList<Temperature> list = new ArrayList<Temperature>();
		for(int i =0; i<c.getCount(); i++) {
			c.moveToPosition(i);
			
			list.add(cursorToTemperature(c));
		}
		
		return list; 
	}

	private Temperature cursorToTemperature(final Cursor c) {
		final Temperature temperature = new Temperature(
				c.getLong(c.getColumnIndexOrThrow("time")),
				c.getFloat(c.getColumnIndexOrThrow("temperature"))
		);
		
		return temperature;
	}

	final static Calendar calendar = Calendar.getInstance();
	
	public ArrayList<Temperature> getMeasurements(Date day) {
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

	public Temperature find(long time, int temperature, int type) {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String whereClause = "time = ? AND temperature = ? AND type = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(time),
				Integer.toString(temperature),
				Integer.toString(type)
		};
		
		final Cursor c = db.query(table, null, whereClause, whereArgs, null, null, null);
		
		if (c.getCount() == 0) {
			return null;
		}
		
		c.moveToFirst();
		return cursorToTemperature(c);
	}
}
