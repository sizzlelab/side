package fi.hut.soberit.sensors;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

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

	public Cursor getSessions() {

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		builder.setTables(DatabaseHelper.SESSION_TABLE);
		
		return builder.query(dbHelper.getReadableDatabase(), null, null, null, null, null, null);
	}
}
