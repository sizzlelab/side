/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.core.storage;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.generic.StorageType;

public class StorageDao {

	private DatabaseHelper dbHelper;

	public StorageDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public long insert(Storage storage) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = StorageTable.valuesFromStorage(storage);
		
		return db.insert(DatabaseHelper.STORAGE_TABLE, "", values);
	}
	
	public int update(Storage storage) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = StorageTable.valuesFromStorage(storage);

		final String whereClause = String.format("%s = ?", StorageTable.ID);
		final String[] whereArgs = new String[] {
			Long.toString(storage.getId())	
		};
		
		return db.update(
				DatabaseHelper.STORAGE_TABLE, 
				values, whereClause, whereArgs);
	}
	
	public Storage get(String url) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final String whereClause = String.format("%s = ?", StorageTable.URL);
		final String[] whereArgs = new String[] { url };

		final Cursor c = db.query(
				DatabaseHelper.STORAGE_TABLE, null, 
				whereClause, whereArgs, null, null, null);
		
		if (c.getCount() == 0) {
			return null;
		}
		return StorageTable.storageFromCursor(c, 0);
	}
	
	public List<Storage> get() {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		final Cursor c = db.query(
				DatabaseHelper.STORAGE_TABLE, 
				null, null, null, null, null, null);
		
		final ArrayList<Storage> list = new ArrayList<Storage>();
		
		for(int i = 0; i<c.getCount(); i++) {
			list.add(StorageTable.storageFromCursor(c, i));
		}
		
		return list;
	}

	public int delete(Storage storage) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();

		final String whereClause = String.format("%s = ?", StorageTable.ID);
		final String[] whereArgs = new String[] { Long.toString(storage.getId()) };
		
		return db.delete(DatabaseHelper.STORAGE_TABLE, whereClause, whereArgs);
	}

	public void deleteOtherThan(List<Long> ids) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String [] selectionArgs = new String [ids.size()];
		for(int i = 0; i<ids.size(); i++) {
			selectionArgs[i] = ids.get(i) + "";
		}
		
		final String selection = Utils.getSetClause(StorageTable.ID, ids, Utils.NOT_IN);
		
		Log.d(StorageDao.class.getSimpleName(), "delete: " + db.delete(DatabaseHelper.STORAGE_TABLE, 
				selection,					
				selectionArgs));
	}
}
