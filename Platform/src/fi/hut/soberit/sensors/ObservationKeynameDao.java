package fi.hut.soberit.sensors;

import java.util.ArrayList;

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
	
	public void updateKeynames(long typeId, ObservationKeyname[] keynames) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		for(ObservationKeyname keyname: keynames) {
			
			final ContentValues values = new ContentValues();
			values.put("observation_type_id", typeId);
			values.put("keyname", keyname.getKeyname());
			values.put("unit", keyname.getUnit());
			values.put("datatype", keyname.getDatatype());
			
			db.insert(DatabaseHelper.OBSERVATION_KEYNAME_TABLE, "", values);
		}
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
			c.move(i);
			
			keynames[i] = c.getLong(0);
		}
				
		return keynames;
	}
}
