package fi.hut.soberit.sensors.core.storagetype;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.core.storage.StorageDao;
import fi.hut.soberit.sensors.generic.StorageType;

public class StorageTypeDao {
	public DatabaseHelper dbHelper;
	
	public StorageTypeDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	public void insert(StorageType storageType) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = StorageTypeTable.valuesFromStorageType(storageType);
		
		db.insert(DatabaseHelper.STORAGE_TYPE_TABLE, "", values);
	}
	
	public void delete(long storageId) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String whereClause = String.format("%s = ?", StorageTypeTable.STORAGE_ID);
		final String [] whereArgs = new String[] { Long.toString(storageId) };
		
		db.delete(DatabaseHelper.STORAGE_TYPE_TABLE, whereClause, whereArgs);
	}
	
	public List<StorageType> get(long storageId) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String whereClause = String.format("%s = ?", StorageTypeTable.STORAGE_ID);
		final String [] whereArgs = new String[] { Long.toString(storageId) };
		
		final Cursor c = db.query(DatabaseHelper.STORAGE_TYPE_TABLE,
				null, whereClause, whereArgs, null, null, null);
		
		final ArrayList<StorageType> types = new ArrayList<StorageType>();
		for(int i = 0; i<c.getCount(); i++) {
			types.add(StorageTypeTable.storageTypeFromCursor(c, i));
		}
		
		return types;
	}

	public boolean exists(StorageType storageType) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final SQLiteStatement st = db.compileStatement(String.format(
				"SELECT count(*) FROM %s WHERE %s = %d AND %s = %d",
				DatabaseHelper.STORAGE_TYPE_TABLE,
				StorageTypeTable.STORAGE_ID,
				storageType.getStorageId(),
				StorageTypeTable.OBSERVATION_TYPE_ID,
				storageType.getObservationTypeId()));
		
		return st.simpleQueryForLong() > 0;
	}
	
	public void deleteOtherThan(List<StorageType> types) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String [] selectionArgs = new String [types.size()*2];
		final StringBuilder clauseBuilder = new StringBuilder();

		for(int i = 0; i<types.size()*2; i+=2) {
			final StorageType type = types.get(i/2);
			selectionArgs[i]	= type.getStorageId() + "";
			selectionArgs[i+1]	= type.getObservationTypeId() + "";
			
			clauseBuilder.append("?, ");
		}
		
		if (types.size() > 0) {
			clauseBuilder.insert(0, "NOT IN (");
			clauseBuilder.setLength(clauseBuilder.length() - 2); // space and comma
			clauseBuilder.append(")");
		
			final String repeatedPart = clauseBuilder.toString(); 
			clauseBuilder.insert(0, StorageTypeTable.STORAGE_ID + " ");
			clauseBuilder.append(" AND ");
			clauseBuilder.append(StorageTypeTable.OBSERVATION_TYPE_ID + " " + repeatedPart);
		}
		
		Log.d(StorageDao.class.getSimpleName(), "delete: " + 
				db.delete(DatabaseHelper.STORAGE_TYPE_TABLE, 
					clauseBuilder.toString(),					
					selectionArgs));
	}

	public List<StorageType> get() {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final Cursor c  = db.query(DatabaseHelper.STORAGE_TYPE_TABLE, null, null, null, null, null, null);
		
		final ArrayList<StorageType> types = new ArrayList<StorageType>();
		
		for(int i =0; i<c.getCount(); i++) {
			types.add(StorageTypeTable.storageTypeFromCursor(c, i));
		}
		c.close();
		
		return types;
	}

}
