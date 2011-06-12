package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;


import fi.hut.soberit.sensors.generic.ObservationType;

public class DriverDao {

	private static final String TAG = DriverDao.class.getSimpleName();
	
	private DatabaseHelper dbHelper;
	
	public DriverDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public long insertDriver(String url) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		ContentValues values = new ContentValues(); 
		values.put("url", url);
				
		return db.insert(DatabaseHelper.DRIVER_TABLE, 
				"", 
				values);		
	}
	
	public long findDriverId(String url) {		
		
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.DRIVER_TABLE);
					
		String		selection = "url = ?";
		String[]	selectionArgs = new String[] {url};
		
		final Cursor c = builder.query(
				dbHelper.getReadableDatabase(), 
				new String[] {"driver_id"}, 
				selection, 
				selectionArgs, 
				null, null, null);
		c.moveToFirst();
		
		if (c.getCount() == 0) {
			return -1;
		}
		
		return c.getLong(0);
	}
	
	public void cleanDrivers() {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.delete(DatabaseHelper.DRIVER_TABLE, null, null);
	}

	public List<DriverInfo> getEnabledDriverList() {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(String.format(
				"%s type " +
				"LEFT JOIN %s driver ON driver.driver_id=type.driver_id ",
				DatabaseHelper.OBSERVATION_TYPE_TABLE,
				DatabaseHelper.DRIVER_TABLE)
				);
				
		final Cursor c = builder.query(
				dbHelper.getReadableDatabase(), 
				new String[] {"driver.driver_id", "url"}, 
				" type.enabled = 1 ", 
				null, null, null, 
				"driver.driver_id ASC");
		
		return multipleDriverInfoFromCursor(c);
	}
	
	public List<DriverInfo> getDriverList() {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.DRIVER_TABLE);
				
		final Cursor c = builder.query(dbHelper.getReadableDatabase(), 
				null, null, null, null, null, 
				"driver_id ASC");
		
		return multipleDriverInfoFromCursor(c);
	}

	private List<DriverInfo> multipleDriverInfoFromCursor(final Cursor c) {
		final ArrayList<DriverInfo> drivers = new ArrayList<DriverInfo>();
			
		for(int i=0; i<c.getCount(); i++) {
			c.moveToPosition(i);
			
			drivers.add(new DriverInfo(
					c.getLong(c.getColumnIndex("driver_id")),
					c.getString(c.getColumnIndex("url")), 
					null)
			);
		}
		
		return drivers;
	}
}
