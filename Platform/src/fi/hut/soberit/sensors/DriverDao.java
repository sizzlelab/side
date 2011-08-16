package fi.hut.soberit.sensors;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.core.DriverTable;

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
	
	public long insertDriver(String url, long driverId) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		ContentValues values = new ContentValues(); 
		values.put("url", url);
		values.put("driver_id", driverId);
				
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

	public List<Driver> getEnabledDriverList() {
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
		
		return DriverTable.multipleDriverInfoFromCursor(c);
	}
	
	public List<Driver> getDriverList() {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.DRIVER_TABLE);
				
		final Cursor c = builder.query(dbHelper.getReadableDatabase(), 
				null, null, null, null, null, 
				"driver_id ASC");
		
		return DriverTable.multipleDriverInfoFromCursor(c);
	}

	public void deleteOtherThan(List<Long> ids) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String [] selectionArgs = new String [ids.size()];
		for(int i = 0; i<ids.size(); i++) {
			selectionArgs[i] = ids.get(i) + "";
		}
		
		final String selection = Utils.getSetClause("driver_id", ids, Utils.NOT_IN);
		
		
		db.delete(DatabaseHelper.DRIVER_TABLE, 
				selection,					
				selectionArgs);

	}

	public Driver getDriverFromType(long observationId) {
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
				" type.observation_type_id = ? ", 
				new String[] {observationId +""}, 
				null, null, 
				"driver.driver_id ASC",
				"1");
		
		return DriverTable.driverInfoFromCursor(c, 0);
	}
}
