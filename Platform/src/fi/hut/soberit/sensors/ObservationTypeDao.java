package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;


import fi.hut.soberit.sensors.generic.ObservationType;

public class ObservationTypeDao {

	private DatabaseHelper dbHelper;
	
	public ObservationTypeDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public long updateType(ObservationType type) {		
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		final ContentValues values = new ContentValues(); 
		values.put("name", type.getName());
		values.put("mimeType", type.getMimeType());
		values.put("description", type.getDescription());
		
		long res = db.insert(DatabaseHelper.OBSERVATION_TYPE_TABLE, 
				"", 
				values);
		
		if (res != -1) {
			return res;
		}

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_TYPE_TABLE);
					
		String selection = null;
		String [] selectionArgs = null;

		final String deviceId = type.getDeviceId();
		if (deviceId == null) {
			selection = "mimeType = '?'";
			selectionArgs = new String[] {type.getMimeType()};
		} else {
			selection = "mimeType = '?' AND device_id = '?'";
			selectionArgs = new String[] {
					type.getMimeType(),
					type.getDeviceId()};
		}
		
		final Cursor c = builder.query(
				db, 
				new String[] {"type_id"},
				selection,
				selectionArgs, 
				null, null, null);
		
		c.moveToFirst();
		return c.getLong(0);
	}
	
	public List<ObservationTypeShort> getObservationTypeShort(long id) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(String.format(
				"%s enabled " +
				"LEFT JOIN %s type ON enabled.observation_type_id=type.observation_type_id ",
				DatabaseHelper.ENABLED_DRIVER_TABLE,
				DatabaseHelper.OBSERVATION_TYPE_TABLE)
				);
		
		final String[] projection = new String[] {
				"type.observation_type_id",
				"type.mimeType"
		};
		
		String selection = "enabled.driver_id = ?";
		String[] selectionArgs = new String[] {Long.toString(id)};
	
		final Cursor c = builder.query(
				db, 
				projection,
				selection,
				selectionArgs, 
				null, null, null);
		
		final ArrayList<ObservationTypeShort> types = new ArrayList<ObservationTypeShort>();
		for(int i = 0; i<c.getCount(); i++) {
			c.move(i);
			
			types.add(new ObservationTypeShort(
					c.getLong(c.getColumnIndex("observation_type_id")),
					c.getString(c.getColumnIndex("mimeType")),
					null));
		}
				
		return types;
	}
}
