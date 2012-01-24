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

public class BloodPressureDao {

	private static final String TAG = BloodPressureDao.class.getSimpleName();
	
	MGDatabaseHelper dbHelper;

    public static final String PRESSURE_CREATE = 
    	"create table pressure (" +
	    	"pressure_id integer primary key, " +
	    	"time DATETIME, " +
	    	"diastolic INTEGER, " +
	    	"systolic INTEGER, " +
	    	"UNIQUE(time, diastolic, systolic) ON CONFLICT REPLACE " +
	    	")";

    public static final String PRESSURE_DROP = 
    	"DROP TABLE IF EXISTS pressure_table";

    public static final String PRESSURE_TABLE = "pressure";

	
	public BloodPressureDao(MGDatabaseHelper dbHelper) {
		super();
		this.dbHelper = dbHelper;
	}
	
	public long insert(BloodPressure record) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("systolic", record.getSystolic());
		values.put("diastolic", record.getDiastolic());
		values.put("time", record.getTime());
		
		return db.insert(PRESSURE_TABLE, "", values);
	}

	public long replace(BloodPressure record) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("systolic", record.getSystolic());
		values.put("diastolic", record.getDiastolic());
		values.put("time", record.getTime());
		
		return db.replace(PRESSURE_TABLE, "", values);
	}

	
	public int update(BloodPressure record) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();

		final ContentValues values = new ContentValues();
		
		values.put("systolic", record.getSystolic());
		values.put("diastolic", record.getDiastolic());
		values.put("time", record.getTime());
		
		final String whereClause = "pressure_id = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(record.getId())
		};
		
		return db.update(PRESSURE_TABLE, values, whereClause, whereArgs);
	}
	
	public ArrayList<BloodPressure> getMeasurements(Date start, Date end) {
		return getMeasurements(start.getTime(), end.getTime());
	}
	
	public ArrayList<BloodPressure> getMeasurements(long start, long end) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "time >= ? AND time <= ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(start),
				Long.toString(end)
		};
		
		final String orderBy = "time ASC";
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		Log.d(TAG, builder.buildQuery(null, whereClause, whereArgs, null, null, null, null) );
		
		final Cursor c = db.query(PRESSURE_TABLE, null, whereClause, whereArgs, null, null, orderBy);
		
		c.moveToFirst();
		
		final ArrayList<BloodPressure> list = new ArrayList<BloodPressure>();
		for(int i =0; i<c.getCount(); i++) {
			c.moveToPosition(i);
			final BloodPressure record = new BloodPressure(
					c.getLong(c.getColumnIndexOrThrow("time")),
					c.getInt(c.getColumnIndexOrThrow("systolic")),
					c.getInt(c.getColumnIndexOrThrow("diastolic"))
			);
			
			record.setId(c.getLong(c.getColumnIndexOrThrow("pressure_id")));
			
			list.add(record);
		}
		
		return list; 
	}
	
	final static Calendar calendar = Calendar.getInstance();
	
	public ArrayList<BloodPressure> getMeasurements(Date day) {
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

	public ArrayList<BloodPressure> getMeasurements() {
		return getMeasurements(0l, Long.MAX_VALUE);
	}
}
