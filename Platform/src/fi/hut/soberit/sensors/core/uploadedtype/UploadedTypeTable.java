/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
