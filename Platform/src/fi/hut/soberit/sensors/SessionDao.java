package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fi.hut.soberit.sensors.generic.Session;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

public class SessionDao {

	DatabaseHelper dbHelper;
	
	public static final String SESSION_CREATE =
        "create table session (" +
	        "session_id integer primary key, " +
	        "start datetime not null," +
	        "end datetime)";    
	
    public static final String SESSION_DROP = 
    	"DROP TABLE IF EXISTS session";
    
    public static final String SESSION_TABLE = "session";
	
	public SessionDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;		
	}

	public long insertSession(long start) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		values.put("start", DatabaseHelper.getUtcDateString(start));
		
		return db.insert(SESSION_TABLE, "", values);
	}
	
	public void updateSession(long sessionId, long end) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		values.put("end", DatabaseHelper.getUtcDateString(end));
		
		String whereClause = SessionsTable.SESSION_ID + " = ? ";
		
		db.update(
				SESSION_TABLE, values, 
				whereClause, 
				new String[] {Long.toString(sessionId)});
	}

	public Cursor getSessions() {

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		builder.setTables(SESSION_TABLE);
		
		return builder.query(dbHelper.getReadableDatabase(), null, null, null, null, null, null);
	}
	
	public List<Session> getSessionObjects() {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		builder.setTables(SESSION_TABLE);
		
		final Cursor c = builder.query(dbHelper.getReadableDatabase(), 
				null, null, null, 
				null, null, null);
		
		ArrayList<Session> list = new ArrayList<Session>();
		
		for(int i = 0; i<c.getCount(); i++) {
			c.moveToPosition(i);
			
			final Date start = DatabaseHelper.getDateFromUtcDateString(c.getString(c.getColumnIndexOrThrow(SessionsTable.START)));
			final String endString = c.getString(c.getColumnIndexOrThrow(SessionsTable.END));
			final Date end = endString != null ? DatabaseHelper.getDateFromUtcDateString(endString) : null;
			long sessionId = c.getLong(c.getColumnIndexOrThrow(SessionsTable.SESSION_ID));
			list.add(new Session(sessionId, start, end));
		}
		c.close();
		
		return list;
	}
}
