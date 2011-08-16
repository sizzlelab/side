package fi.hut.soberit.sensors;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fi.hut.soberit.sensors.core.ObservationValueTable;
import fi.hut.soberit.sensors.generic.GenericObservation;

public class ObservationValueDao {
	private DatabaseHelper dbHelper;

	public ObservationValueDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public long insertObservationValue(GenericObservation observation) {		
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		final ContentValues values = new ContentValues(); 
		values.put("observation_type_id", observation.getObservationTypeId());
		values.put("time", observation.getTime());
		values.put("value", observation.getValue());
		
		return db.insert(DatabaseHelper.OBSERVATION_VALUE_TABLE, 
				"", 
				values);
	}

	public GenericObservation find(long typeId, long time, byte[] value) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = "observation_type_id = ? AND time = ?";
		
		final String [] whereArgs = new String [] {
				Long.toString(typeId),
				Long.toString(time)
		};
		
		final Cursor c = db.query(
				DatabaseHelper.OBSERVATION_VALUE_TABLE, null, 
				whereClause, whereArgs, null, null, null);
		
		if (c.getCount() == 0) {
			return null;
		}
		
		for(int i = 0; i<c.getCount(); i++) {
			final GenericObservation observation = ObservationValueTable.observationFromCursor(c, i);
			if (observation.getValue().equals(value)) {
				return observation;
			}
		}

		throw new RuntimeException("Method pathetically fails to return what is expected! ");
	}

	public ArrayList<GenericObservation> getAll() {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final Cursor c = db.query(
				DatabaseHelper.OBSERVATION_VALUE_TABLE, null, 
				null, null, null, null, "time DESC");
		
		final ArrayList<GenericObservation> list = new ArrayList<GenericObservation>();
		for(int i = 0; i<c.getCount(); i++) {
			list.add(ObservationValueTable.observationFromCursor(c, i));
		}
		
		return list;
	}
}
