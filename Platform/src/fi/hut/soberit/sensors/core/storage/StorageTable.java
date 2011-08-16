package fi.hut.soberit.sensors.core.storage;

import android.content.ContentValues;
import android.database.Cursor;
import fi.hut.soberit.sensors.generic.Storage;

public class StorageTable {

	public static final String ID = "storage_id";
	public static final String URL = "url";

	public static Storage storageFromCursor(Cursor c, int i) {
		c.moveToPosition(i);
		
		Storage storage = new Storage(
				c.getLong(c.getColumnIndexOrThrow(ID)),
				c.getString(c.getColumnIndexOrThrow(URL)));
		
		return storage;
	}
		
	public static ContentValues valuesFromStorage(Storage storage) {
		
		ContentValues values = new ContentValues(); 
		values.put(ID, storage.getId());
		values.put(URL, storage.getUrl());
		
		return values;
	}
}
