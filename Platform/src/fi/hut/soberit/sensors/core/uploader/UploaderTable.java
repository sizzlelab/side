/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
