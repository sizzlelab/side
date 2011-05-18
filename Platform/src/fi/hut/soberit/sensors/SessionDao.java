package fi.hut.soberit.sensors;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class SessionDao {

	DatabaseHelper dbHelper;
	
	public SessionDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;		
	}

	public long insertSession(long start) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		values.put("start", start);
		
		return db.insert(DatabaseHelper.SESSION_TABLE, "", values);
	}
}
