package fi.hut.soberit.sensors.core.uploadedtype;

import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.generic.UploadedType;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class UploadedTypeTable {
	
	public final static String AUTHORITY = "fi.hut.soberit.sensors.core.uploadedtype";

	public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/uploaded_types");
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soberit.uploaded_type";

    public static final String CONTENT_TYPES = "vnd.android.cursor.item/vnd.soberit.uploaded_type";
	
    public static final String MIME_TYPE = "mime_type";

    public static final String UPLOADER_ID = "uploader_id";
    
    public static final String ENABLED = "enabled";

	public static final String UPLOADED_TYPE_ID = "uploaded_type_id";

	public static UploadedType uploadedTypeFromCursor(Cursor c, int i) {
		c.moveToPosition(i);
		
		UploadedType type = new UploadedType(
				c.getString(c.getColumnIndex(MIME_TYPE)),
				c.getLong(c.getColumnIndex(UPLOADER_ID)));
		
		return type;
	}
		
	public static ContentValues valuesFromUploadedType(UploadedType type) {
		
		ContentValues values = new ContentValues(); 
		values.put("mime_type", type.getMimeType());
		values.put("uploader_id", type.getUploaderId());
		if (type.getId() != -1) {
			values.put("uploaded_type_id", type.getId());
		}
		
		return values;
	}
}
