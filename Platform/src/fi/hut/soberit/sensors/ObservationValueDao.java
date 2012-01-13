/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
	
	public long replaceObservationValue(GenericObservation observation) {		
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		final ContentValues values = new ContentValues(); 
		values.put("observation_type_id", observation.getObservationTypeId());
		values.put("time", observation.getTime());
		values.put("value", observation.getValue());
		
		return db.replace(DatabaseHelper.OBSERVATION_VALUE_TABLE, 
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
