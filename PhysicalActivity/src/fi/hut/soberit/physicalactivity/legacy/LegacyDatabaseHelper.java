package fi.hut.soberit.physicalactivity.legacy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import eu.mobileguild.db.MGDatabaseHelper;


public class LegacyDatabaseHelper extends MGDatabaseHelper {

    public static final int DATABASE_VERSION = 5;
	
    public static String getDbName(long sessionId) {
    	return "legacy" + sessionId + ".sqlite";
    }
	
	public LegacyDatabaseHelper(Context context, long sessionId) {
		super(context, getDbName(sessionId), DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("LegacyDatabaseHelper", "onCreate");
        db.execSQL(LegacyObservationDao.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(LegacyObservationDao.DROP_TABLE);
	}

}
