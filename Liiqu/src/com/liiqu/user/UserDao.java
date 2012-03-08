package com.liiqu.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import eu.mobileguild.db.MGDatabaseHelper;

public class UserDao {

	private static final String TAG = UserDao.class.getSimpleName();

    public static final String USER_CREATE = 
    	"create table user (" +
	    	"user_id integer primary key, " +
    		"liiqu_user_id NUMBER, " +
	    	"name TEXT, " +
	    	"picture TEXT," +
	    	"UNIQUE(liiqu_user_id) ON CONFLICT REPLACE " +
	    	")";
	
    public static final String USER_TABLE = "user";

    public static final String USER_DROP = "DROP TABLE IF EXISTS user";

	private MGDatabaseHelper dbHelper;
		
	public UserDao(MGDatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public long insert(User user) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		
		values.put("liiqu_user_id", user.liiquUserId);
		values.put("name", user.name);
		values.put("picture", user.picture);
		
		return db.insert(USER_TABLE, "", values);
	}

	public long replace(User user) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues(	);
		
		values.put("liiqu_user_id", user.liiquUserId);
		values.put("name", user.name);
		values.put("picture", user.picture);

		return db.replace(USER_TABLE, "", values);
	}	
	
	public User getUser(Long liiquUserId) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "liiqu_user_id = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(liiquUserId)
		};
		
//		final String orderBy = "startTime ASC";
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		Log.d(TAG, builder.buildQuery(null, whereClause, whereArgs, null, null, null, null) );
		
		final Cursor c = db.query(USER_TABLE, null, whereClause, whereArgs, null, null, null);
		
		c.moveToFirst();
		
		if (c.getCount() == 0) {
			return null;
		}
		
		return cursorToUser(c); 
	}

	private User cursorToUser(final Cursor c) {
		final User e = new User();
		
		e.liiquUserId = c.getLong(c.getColumnIndexOrThrow("liiqu_user_id"));
		e.name = c.getString(c.getColumnIndexOrThrow("name"));
		e.picture = c.getString(c.getColumnIndexOrThrow("picture"));
		
		return e;
	}
}
