package eu.mobileguild.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class MGDatabaseHelper extends SQLiteOpenHelper {

	protected final String TAG = this.getClass().getSimpleName();
	
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private SQLiteDatabase readOnlyDatabase;

	private SQLiteDatabase readWriteDatabase;
    
    final static Calendar calendar;
    static {
    	calendar = Calendar.getInstance();
    }
    
	public MGDatabaseHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}
    
	public static String getUtcDateString(Date date) {
		return getUtcDateString(date.getTime());
	}   
	
	public static String getUtcDateString(long millis) {
		final int timezoneOffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		return dateFormat.format(millis - timezoneOffset);
	}

	public static long getLongFromUtcDateString(String time) {
		final int timezoneOffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		try {
			return dateFormat.parse(time).getTime() + timezoneOffset;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static Date getDateFromUtcDateString(String time) {
		final int timezoneOffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		try {
			final Date intermediary = dateFormat.parse(time);
			intermediary.setTime(intermediary.getTime() + timezoneOffset);
			return intermediary;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SQLiteDatabase getReadableDatabase() {
		if (readOnlyDatabase == null) {
			readOnlyDatabase = super.getReadableDatabase();
		}
		
		return readOnlyDatabase;
	}

	public SQLiteDatabase getWritableDatabase()  {
		if (readWriteDatabase == null) {
			readWriteDatabase = super.getWritableDatabase();
		}
		return readWriteDatabase;
	}

	public void closeDatabases() {
		readOnlyDatabase = null;		
		readWriteDatabase = null;
		
		close();
	}
}
