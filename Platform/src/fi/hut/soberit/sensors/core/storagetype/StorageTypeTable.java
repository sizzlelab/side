package fi.hut.soberit.sensors.core.storagetype;

import android.content.ContentValues;
import android.database.Cursor;
import fi.hut.soberit.sensors.generic.StorageType;

public class StorageTypeTable {
	public static final String STORAGE_ID = "storage_id";
	public static final String OBSERVATION_TYPE_ID = "observation_type_id";

	public static StorageType storageTypeFromCursor(Cursor c, int i) {
		c.moveToPosition(i);
		
		final StorageType storageType = new StorageType(
				c.getLong(c.getColumnIndexOrThrow(STORAGE_ID)),
				c.getLong(c.getColumnIndexOrThrow(OBSERVATION_TYPE_ID)));
		
		return storageType;
	}
		
	public static ContentValues valuesFromStorageType(StorageType storageType) {
		
		ContentValues values = new ContentValues(); 
		values.put(STORAGE_ID, storageType.getStorageId());
		values.put(OBSERVATION_TYPE_ID, storageType.getObservationTypeId());
		
		return values;
	}
}
