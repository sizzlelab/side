package fi.hut.soberit.sensors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "sessions.sqlite";
    private static final String SHARD_NAME = "shard";    
    
    public static final int DATABASE_VERSION = 5;

    static final String OBSERVATION_TYPE_CREATE = 
    	"create table observation_type (" +
	    	"observation_type_id integer primary key, " +
	    	"name TEXT, " +
	    	"mimeType TEXT, " +
	    	"deviceId TEXT, " +
	    	"description TEXT )";

    static final String OBSERVATION_KEYNAME_CREATE = 
    	"create table observation_keyname (" +
	    	"observation_keyname_id integer primary key, " +
	    	"observation_type_id INTEGER, " +
	    	"keyname TEXT, " +
	    	"unit TEXT, " +
	    	"datatype TEXT)";
    
    static final String OBSERVATION_VALUE_CREATE = 
    	"create table observation_value (" +
    		"observation_value_id INTEGER primary key, " +
    		"observation_keyname_id INTEGER," +
    		"observation_type_id INTEGER," +
    		"time INTEGER(8), " +
    		"value BLOB)";

    
    static final String DRIVER_CREATE = 
    	"create table driver (" +
    		"driver_id INTEGER primary key, " +
    		"url TEXT)";
    
    static final String ENABLED_DRIVER_CREATE = 
    	"create table enabled_driver (" +
    		"driver_id INTEGER, " +
    		"observation_type_id INTEGER)";
    
    static final String OBSERVATION_VALUE_INDEX_CREATE = 
    	"CREATE UNIQUE INDEX i_observations_time_type " +
    	"ON observation_value(time, observation_type_id);";
    
    static final String SESSION_CREATE =
        "create table session (" +
	        "session_id integer primary key, " +
	        "start datetime not null," +
	        "end datetime);";    
    
    static final String OBSERVATION_TYPE_DROP = 
    	"DROP TABLE IF EXISTS observation_type";

    static final String OBSERVATION_KEYNAME_DROP = 
    	"DROP TABLE IF EXISTS observation_keyname";

    static final String OBSERVATION_VALUE_DROP = 
    	"DROP TABLE IF EXISTS observation_value";
    
    static final String OBSERVATION_VALUE_INDEX_DROP = 
    	"DROP INDEX IF EXISTS i_observations_time_type";
    
    static final String DRIVER_DROP = "DROP TABLE IF EXISTS driver";
    
    static final String ENABLED_DRIVER_DROP = "DROP TABLE IF EXISTS enabled_driver";

    
    static final String SESSION_DROP = 
    	"DROP TABLE IF EXISTS session";
    
    public static final String SESSION_TABLE = "sessions";

    public static final String OBSERVATION_TYPE_TABLE = "observation_type";
    
    public static final String OBSERVATION_KEYNAME_TABLE = "observation_keyname";

    public static final String OBSERVATION_VALUE_TABLE = "observation_value";
    
    public static final String DRIVER_TABLE = "driver";
    
    public static final String ENABLED_DRIVER_TABLE = "enabled_driver";

    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static final String TAG = DatabaseHelper.class.getSimpleName();
	
	private SQLiteDatabase readOnlyDatabase;

	private SQLiteDatabase readWriteDatabase;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, long sessionId) {
        super(context, shardName(sessionId), null, DATABASE_VERSION);
        Log.d(TAG, "Opening shard " + sessionId);
    }

    public static String shardName(long sessionId) {
		return SHARD_NAME + sessionId + ".sqlite";
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(OBSERVATION_TYPE_CREATE);
        db.execSQL(OBSERVATION_KEYNAME_CREATE);
        db.execSQL(OBSERVATION_VALUE_CREATE);
        db.execSQL(OBSERVATION_VALUE_INDEX_CREATE);
        db.execSQL(SESSION_CREATE);
        db.execSQL(DRIVER_CREATE);
        db.execSQL(ENABLED_DRIVER_CREATE);
	}
	
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        
        db.execSQL(OBSERVATION_TYPE_DROP);
        db.execSQL(OBSERVATION_KEYNAME_DROP);
        db.execSQL(OBSERVATION_VALUE_DROP);
        db.execSQL(OBSERVATION_VALUE_INDEX_DROP);
        db.execSQL(SESSION_DROP);
        db.execSQL(DRIVER_DROP);
        db.execSQL(ENABLED_DRIVER_DROP);

        onCreate(db);
    }

    final static Calendar calendar;
    static {
    	calendar = Calendar.getInstance();
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
	
	public static void upgradeDatabase(Context context) {
		new DatabaseHelper(context).getWritableDatabase().close();
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