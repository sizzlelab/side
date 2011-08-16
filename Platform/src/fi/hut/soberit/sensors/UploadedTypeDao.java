package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.core.uploadedtype.UploadedTypeTable;
import fi.hut.soberit.sensors.core.uploader.UploaderTable;
import fi.hut.soberit.sensors.generic.UploadedType;
import fi.hut.soberit.sensors.generic.Uploader;

// TODO: move all the database logic into one class (merge Dao and table classes)
public class UploadedTypeDao {
	private static final String TAG = UploadedTypeDao.class.getSimpleName();
	
	private DatabaseHelper dbHelper;
	
	public UploadedTypeDao(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}
	
	
	public UploadedType findType(String mimeType, long uploaderId) {		
		
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.UPLOADED_TYPE_TABLE);
					
		String		selection = "mime_type = ? AND uploader_id = ?";
		String[]	selectionArgs = new String[] {mimeType, String.valueOf(uploaderId)};
		
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
		
		return UploadedTypeTable.uploadedTypeFromCursor(c, 0);
	}

	public void deleteAll(long uploader_id) {
		
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.delete(dbHelper.UPLOADED_TYPE_TABLE, "uploader_id = ?", new String[] {String.valueOf(uploader_id)});
	}

	public long insert(UploadedType type) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = UploadedTypeTable.valuesFromUploadedType(type);
		
		return db.insert(dbHelper.UPLOADED_TYPE_TABLE, "", values);
	}


	public List<UploadedType> getTypes(long id) {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.UPLOADED_TYPE_TABLE);
					
		String		selection = "uploader_id = ?";
		String[]	selectionArgs = new String[] {String.valueOf(id)};
		
		final Cursor c = builder.query(
				dbHelper.getReadableDatabase(), 
				null, 
				selection, 
				selectionArgs, 
				null, null, null);
		
		final ArrayList<UploadedType> uploaders = new ArrayList<UploadedType>();
		
		// needed for getCount
		c.moveToFirst();
		for(int i = 0; i<c.getCount(); i++) {
			uploaders.add(UploadedTypeTable.uploadedTypeFromCursor(c, i));	
		}
		
		return uploaders;
	}


	public void deleteOtherThanBelongingTo(List<Long> ids) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final String [] selectionArgs = new String [ids.size()];
		for(int i = 0; i<ids.size(); i++) {
			selectionArgs[i] = ids.get(i) + "";
		}
		
		final String selection = Utils.getSetClause("uploader_id", ids, Utils.NOT_IN);
		
		db.delete(DatabaseHelper.UPLOADED_TYPE_TABLE, 
				selection,					
				selectionArgs);		
	}
}
