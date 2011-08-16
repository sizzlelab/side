package fi.hut.soberit.sensors.core.uploader;

import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.generic.Uploader;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;


public class UploaderTable {

	public final static String AUTHORITY = "fi.hut.soberit.sensors.core.uploader";

	public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/uploaders");
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soberit.uploader";

    public static final String CONTENT_TYPES = "vnd.android.cursor.item/vnd.soberit.uploader";
		
    public static final String UPLOADER_ID = "uploader_id";
    
    public static final String NAME = "name";

    public static final String ENABLED = "enabled";
    
    public static final String URL = "url";
    
	public static Uploader uploaderFromCursor(final Cursor c, int pos) {
		c.moveToPosition(pos);
			
		final Uploader uploader = new Uploader(
				c.getString(c.getColumnIndex("name")),
				c.getString(c.getColumnIndex("url")));
		uploader.setId(c.getLong(c.getColumnIndex(UPLOADER_ID)));
		
		uploader.setEnabled(Utils.getBooleanFromDBInt(c, ENABLED));
		return uploader;
	}	
	
	public static ContentValues valuesFromUploader(Uploader uploader) {
		ContentValues values = new ContentValues();
		
		values.put("url", uploader.getUrl());
		values.put("name", uploader.getName());
		
		if (uploader.getId() != -1) {
			values.put("uploader_id", uploader.getId());
		}
		values.put("enabled", uploader.isEnabled() ? 1 : 0);
		
		return values;
	}
}
