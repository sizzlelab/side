package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import fi.hut.soberit.sensors.generic.ObservationKeyname;

public class ObservationKeynameDao {

	final DatabaseHelper dbHelper;
	
	public ObservationKeynameDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public long insertKeyname(ObservationKeyname keyname) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();

		final ContentValues values = new ContentValues();
		values.put("observation_type_id", keyname.getObservationTypeId());
		values.put("keyname", keyname.getKeyname());
		values.put("unit", keyname.getUnit());
		values.put("datatype", keyname.getDatatype());
		
		return db.insert(DatabaseHelper.OBSERVATION_KEYNAME_TABLE, "", values);		
	}
	
	public int updateKeyname(ObservationKeyname keyname) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
			
		final String selection = "keyname = ? AND observation_type_id = ?";
		final String [] selectionArgs = new String[] {
				keyname.getKeyname(), 
				Long.toString(keyname.getObservationTypeId())};
		
		final ContentValues values = new ContentValues();
		values.put("unit", keyname.getUnit());
		values.put("datatype", keyname.getDatatype());
		
		return db.update(DatabaseHelper.OBSERVATION_KEYNAME_TABLE, 
				values, 
				selection, 
				selectionArgs);
	}
	
	public long[] getKeynamesShort(long id) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_KEYNAME_TABLE);
		
		final String[] projection = new String[] {"observation_keyname_id"};
		
		String selection = "observation_type_id = ?";
		String[] selectionArgs = new String[] {Long.toString(id)};
	
		final Cursor c = builder.query(
				db, 
				projection,
				selection,
				selectionArgs, 
				null, null, null);
		
		final long[] keynames = new long[c.getCount()];
		for(int i = 0; i<c.getCount(); i++) {
			c.moveToPosition(i);
			
			keynames[i] = c.getLong(c.getColumnIndex("observation_keyname_id"));
		}
				
		return keynames;
	}

	public ObservationKeyname[] getKeynames(long id) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_KEYNAME_TABLE);
				
		String selection = "observation_type_id = ?";
		String[] selectionArgs = new String[] {Long.toString(id)};
	
		final Cursor c = builder.query(
				db, 
				null,
				selection,
				selectionArgs, 
				null, null, null);
		
		final ObservationKeyname [] keynames = new ObservationKeyname [c.getCount()];
		
		for(int i = 0; i<c.getCount(); i++) {			
			keynames[i] = observationToKeyname(c, i);
		}
				
		return keynames;
	}
	
	
	
	private ObservationKeyname observationToKeyname(Cursor c, int pos) {
		c.moveToPosition(pos);
		
		final ObservationKeyname keyname = new ObservationKeyname(
			c.getString(c.getColumnIndex("keyname")),
			c.getString(c.getColumnIndex("unit")),
			c.getString(c.getColumnIndex("datatype"))
		);
		
		keyname.setId(c.getLong(c.getColumnIndex("observation_keyname_id")));
		keyname.setObservationTypeId(c.getLong(c.getColumnIndex("observation_type_id")));		
		
		return keyname;
	}

	public long findKeynameId(long typeId, String keyname) {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_KEYNAME_TABLE);
					
		final String selection = "keyname = ? AND observation_type_id = ?";
		final String [] selectionArgs = new String[] {
				keyname, 
				Long.toString(typeId)};
		
		final Cursor c = builder.query(
				dbHelper.getWritableDatabase(), 
				new String[] {"observation_type_id"},
				selection,
				selectionArgs, 
				null, null, null);
		
		c.moveToFirst();
		
		if (c.getCount() == 0) {
			return -1;
		}
		
		return c.getLong(0);
	}
}
