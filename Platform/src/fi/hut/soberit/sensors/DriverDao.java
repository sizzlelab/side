package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;


import fi.hut.soberit.sensors.generic.ObservationType;

public class DriverDao {

	private DatabaseHelper dbHelper;
	
	public DriverDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public long insertDriver(String url) {		
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		ContentValues values = new ContentValues(); 
		values.put("url", url);
				
		long driverId = db.insert(DatabaseHelper.DRIVER_TABLE, 
				"", 
				values);
		
		if (driverId != -1) {
			return driverId;
		}
		
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.DRIVER_TABLE);
					
		String		selection = "url = '?'";
		String[]	selectionArgs = new String[] {url};
		
		final Cursor c = builder.query(db, 
				new String[] {"driver_id"}, 
				selection, 
				selectionArgs, 
				null, null, null);
		c.moveToFirst();
		
		return c.getLong(0);
	}
	
	public void insertEnabledDriver(long driverId, long[] types) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 

		for(Long type: types) {
			final ContentValues values = new ContentValues();
			values.put("driver_id", driverId);
			values.put("observation_type_id", type);
			
			db.insert(DatabaseHelper.ENABLED_DRIVER_TABLE, "", values);
		}
	}
	
	public void cleanEnabledDrivers() {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.delete(DatabaseHelper.ENABLED_DRIVER_TABLE, null, null);
	}
	
	public void cleanDrivers() {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.delete(DatabaseHelper.DRIVER_TABLE, null, null);
	}

	public List<DriverInfo> getEnabledDriverList() {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(String.format(
				"%s enabled " +
				"LEFT JOIN %s driver ON driver.driver_id=enabled.driver_id ",
				DatabaseHelper.ENABLED_DRIVER_TABLE,
				DatabaseHelper.DRIVER_TABLE)
				);
				
		final Cursor c = builder.query(db, null, null, null, null, null, null);
		
		final ArrayList<DriverInfo> drivers = new ArrayList<DriverInfo>();
		
		for(int i=0; i<c.getCount(); i++) {
			c.move(i);
			drivers.add(new DriverInfo(
					c.getLong(c.getColumnIndex("driver_id")),
					c.getString(c.getColumnIndex("url")), 
					null)
			);
		}
		
		return drivers;
	}
}
