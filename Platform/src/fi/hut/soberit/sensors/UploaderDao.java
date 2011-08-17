/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
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

import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.core.uploader.UploaderTable;
import fi.hut.soberit.sensors.generic.Uploader;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

public class UploaderDao {
	private static final String TAG = UploaderDao.class.getSimpleName();
	
	private DatabaseHelper dbHelper;
	
	public UploaderDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public long insertUploader(Uploader uploader) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		ContentValues values = UploaderTable.valuesFromUploader(uploader);
				
		return db.insert(DatabaseHelper.UPLOADER_TABLE, 
				"", 
				values);		
	}
	
	
	public Uploader findUploader(String url) {		
		
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.UPLOADER_TABLE);
					
		String		selection = "url = ?";
		String[]	selectionArgs = new String[] {url};
		
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
		
		return UploaderTable.uploaderFromCursor(c, 0);
	}
	
	public void deleteAll(long uploader_id) {
		
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.delete(dbHelper.UPLOADER_TABLE, "uploader_id = ?", new String[] {String.valueOf(uploader_id)});
	}

	public ArrayList<Uploader> getUploaders(Boolean enabled) {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.UPLOADER_TABLE);
					
		String selection = null;
		String[] selectionArgs = null;
		
		if (enabled != null) {
			selection = "enabled = ?";
			selectionArgs = new String[] {Integer.toString(enabled ? 1 : 0)};
		}
		
		final Cursor c = builder.query(
				dbHelper.getReadableDatabase(), 
				null, 
				selection, 
				selectionArgs, 
				null, null, null);
		
		final ArrayList<Uploader> uploaders = new ArrayList<Uploader>();
		
		// needed for getCount
		c.moveToFirst();
		for(int i = 0; i<c.getCount(); i++) {
			uploaders.add(UploaderTable.uploaderFromCursor(c, i));	
		}
		
		return uploaders;
	}

	public void deleteOtherThan(List<Long> ids) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String [] selectionArgs = new String [ids.size()];
		for(int i = 0; i<ids.size(); i++) {
			selectionArgs[i] = ids.get(i) + "";
		}
		
		final String selection = Utils.getSetClause("uploader_id", ids, Utils.NOT_IN);
		
		db.delete(DatabaseHelper.UPLOADER_TABLE, 
				selection,					
				selectionArgs);		
	}

	public int updateType(Uploader type) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase(); 
		
		final ContentValues values = UploaderTable.valuesFromUploader(type);
		
		final String whereClause = "url = ? AND uploader_id = ?";
		final String[] whereClauseArgs = new String[] {
				type.getUrl(),
				Long.toString(type.getId())
		} ;
		
		return db.update(
				DatabaseHelper.UPLOADER_TABLE, 
				values, 
				whereClause, 
				whereClauseArgs);
		
	}
}
