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
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.core.DriverTable;
import fi.hut.soberit.sensors.core.ObservationTypeTable;
import fi.hut.soberit.sensors.generic.ObservationType;

public class ObservationTypeDao {

	private DatabaseHelper dbHelper;
	
	public ObservationTypeDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public long insertType(ObservationType type) {		
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		final ContentValues values = new ContentValues(); 
		values.put("driver_id", type.getDriver().getId());
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
				Long.toString(type.getDriver().getId())
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
		
		return fi.hut.soberit.sensors.core.ObservationTypeTable.observationTypeFromCursor(c, 0);
	}

	public ObservationType findType(String mimeType, String driverUrl) {

		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(String.format("%s type LEFT JOIN %s driver ON type.%s=driver.%s",
				DatabaseHelper.OBSERVATION_TYPE_TABLE,
				DatabaseHelper.DRIVER_TABLE,
				ObservationTypeTable.DRIVER_ID,
				DriverTable.DRIVER_ID));
					
		String selection = null;
		String [] selectionArgs = null;

		selection = String.format("%s = ? AND %s = ?", 
				ObservationTypeTable.MIME_TYPE, 
				DriverTable.URL);
		selectionArgs = new String[] {mimeType, driverUrl};
		
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
		
		final ObservationType type = ObservationTypeTable.observationTypeFromCursor(c, 0, null);
		c.close();
		
		return type;
	}

	
	// driver ids are used to filter observation types for only existing drivers, as
	// when driver list is refreshed, observation_types & etc are preserved.
	public List<ObservationType> getObservationTypes(ArrayList<Long> ids, Boolean enabled) {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_TYPE_TABLE);
			
		final StringBuilder selectionBuilder = new StringBuilder();
		final String[] selectionArgs = ids != null ? new String[ids.size()] : null;
		
		if (enabled != null) {
			selectionBuilder.append(enabled ? "enabled = 1 " : "enabled = 0");
		}
		
		if (ids != null) {
			if (enabled != null && ids.size() > 0) {
				selectionBuilder.append(" AND ");
			}
			
			if (ids.size() > 0) {
				selectionBuilder.append(" driver_id IN (");
			}
			
			for (int i = 0; i< ids.size(); i++) {
				selectionBuilder.append("?, ");
				selectionArgs[i] = ids.get(i) + "";
			}
			
			if (ids.size() > 0) {
				selectionBuilder.setLength(selectionBuilder.length() -2);
				selectionBuilder.append(")");
			}
		}
		
		final Cursor c = builder.query(
				dbHelper.getReadableDatabase(), 
				null,
				selectionBuilder.toString(),
				selectionArgs, 
				null, null, null);
		
		List<ObservationType> types = new ArrayList<ObservationType>();
		
		for(int i = 0; i<c.getCount(); i++) {
			types.add(fi.hut.soberit.sensors.core.ObservationTypeTable.observationTypeFromCursor(c, i));
		}
		
		return types;
	}
	
	
	

	public List<ObservationType> getObservationType(long driverId, Boolean enabled) {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.OBSERVATION_TYPE_TABLE);
					
		String selection = null;
		
		String[] selectionArgs = null;
		
		if (enabled == null) {
			selection = "driver_id = ?";
			selectionArgs = new String [] {Long.toString(driverId)};
		} else {
			selection = "driver_id = ? AND enabled = ? ";
			
			selectionArgs = new String [] {
					Long.toString(driverId),
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
			types.add(fi.hut.soberit.sensors.core.ObservationTypeTable.observationTypeFromCursor(c, i));
		}
		
		return types;
	}

	public void deleteOtherThan(List<Long> ids) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String [] selectionArgs = new String [ids.size()];
		for(int i = 0; i<ids.size(); i++) {
			selectionArgs[i] = ids.get(i) + "";
		}
		
		final String selection = Utils.getSetClause("observation_type_id", ids, Utils.NOT_IN);
		
		db.delete(DatabaseHelper.OBSERVATION_TYPE_TABLE, 
				selection,					
				selectionArgs);
	}
}

