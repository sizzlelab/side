package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.generic.ObservationType;

public class ObservationTypeDao {

	private DatabaseHelper dbHelper;
	
	public ObservationTypeDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public long insertType(ObservationType type) {		
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		final ContentValues values = new ContentValues(); 
		values.put("driver_id", type.getDriverId());
		values.put("name", type.getName());
		values.put("mimeType", type.getMimeType());
		values.put("description", type.getDescription());
		values.put("enabled", type.isEnabled());
		
		return db.insert(DatabaseHelper.OBSERVATION_TYPE_TABLE, 
				"", 
				values);
	}
	
	public long updateType(ObservationType type) {		
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		final ContentValues values = new ContentValues(); 
		values.put("name", type.getName());
		values.put("mimeType", type.getMimeType());
		values.put("description", type.getDescription());
		values.put("enabled", type.isEnabled());
		
		final String whereClause = "mimeType = ? AND driver_id = ?";
		final String[] whereClauseArgs = new String[] {
				type.getMimeType(),
				Long.toString(type.getDriverId())
		} ;
		
		return db.update(
				DatabaseHelper.OBSERVATION_TYPE_TABLE, 
				values, 
				whereClause, 
				whereClauseArgs);
	}
	
	public ObservationType findType(String mimeType, long driverId) {

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_TYPE_TABLE);
					
		String selection = null;
		String [] selectionArgs = null;

		selection = "mimeType = ? AND driver_id = ?";
		selectionArgs = new String[] {
				mimeType, 
				Long.toString(driverId)};
		
		final Cursor c = builder.query(
				dbHelper.getReadableDatabase(), 
				null,
				selection,
				selectionArgs, 
				null, null, null);
		
		c.moveToFirst();
		
		if (c.getCount() == 0) {
			return null;
		}
		return observationTypeFromCursor(c, 0);
	}
	
	public List<ObservationType> getEnabledObservationTypes() {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_TYPE_TABLE);
					
		String selection = "enabled = 1";
		
		final Cursor c = builder.query(
				dbHelper.getReadableDatabase(), 
				null,
				selection,
				null, 
				null, null, null);
		
		List<ObservationType> types = new ArrayList<ObservationType>();
		
		for(int i = 0; i<c.getCount(); i++) {
			types.add(observationTypeFromCursor(c, i));
		}
		
		return types;
	}
	
	
	private ObservationType observationTypeFromCursor(final Cursor c, int pos) {
		c.moveToPosition(pos);
		
		final ObservationType type = new ObservationType(
				c.getString(c.getColumnIndex("name")),
				c.getString(c.getColumnIndex("mimeType")),
				c.getString(c.getColumnIndex("description")),
				null);
		
		type.setEnabled(Utils.getBooleanFromDBInt(c, "enabled"));
		type.setId(c.getLong(c.getColumnIndex("observation_type_id")));
		type.setDriverId(c.getLong(c.getColumnIndex("driver_id")));
		
		return type;
	}

	public List<ObservationType> getObservationType(long id, Boolean enabled) {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_TYPE_TABLE);
					
		String selection = null;
		
		String[] selectionArgs = null;
		
		if (enabled == null) {
			selection = "driver_id = ?";
			selectionArgs = new String [] {Long.toString(id)};
		} else {
			selection = "driver_id = ? AND enabled = ? ";
			
			selectionArgs = new String [] {
					Long.toString(id),
					enabled ? "1" : "0"
			};  
		}
		
		final Cursor c = builder.query(
				dbHelper.getReadableDatabase(), 
				null,
				selection,
				selectionArgs, 
				null, null, null);
		
		List<ObservationType> types = new ArrayList<ObservationType>();
		
		for(int i = 0; i<c.getCount(); i++) {
			types.add(observationTypeFromCursor(c, i));
		}
		
		return types;
	}
}

