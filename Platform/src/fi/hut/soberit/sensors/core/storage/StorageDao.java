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
