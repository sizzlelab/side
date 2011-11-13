package fi.hut.soberit.physicalactivity.legacy;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;


public class LegacyObservationDao {
    public static final String CREATE_TABLE = 
            "create table observations (" +
	            "_id integer primary key, " +
	            "session_id integer not null, " +
	            "time datetime not null," +
	            "type integer not null, " +
	            "observation1 real not null, " +
	            "observation2 real, " +
	            "observation3 real," +
	            "after_timeout integer);";
	    
    public static final String TABLE_NAME = "observations";

    public static final String DROP_TABLE = 
    	"DROP TABLE IF EXISTS observations";

	private LegacyDatabaseHelper dbHelper;
        
    public LegacyObservationDao(LegacyDatabaseHelper dbHelper) {
    	this.dbHelper = dbHelper;
    }

    public long insert(LegacyObservation observation) {
    	final SQLiteDatabase db = dbHelper.getWritableDatabase();
    
    	final ContentValues values = new ContentValues();
    	
    	values.put("session_id", observation.getSessionId());
    	values.put("time", observation.getTime());
    	values.put("type", observation.getType().ordinal());
    	values.put("observation1", observation.getObservation1());
    	values.put("observation2", observation.getObservation2());
    	values.put("observation3", observation.getObservation3());
    	
    	return db.insert(TABLE_NAME, "", values);
    }
        
}
