/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.core;

import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.generic.ObservationType;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ObservationTypeTable {

    public static final String AUTHORITY = "fi.hut.soberit.sensors.core";

	public static final Uri CONTENT_URI = Uri.parse("content://fi.hut.soberit.sensors.core/observation_types");
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soberit.observation_type";

    public static final String CONTENT_TYPES = "vnd.android.cursor.item/vnd.soberit.observation_type";
	
	
    public static final String OBSERVATION_TYPE_ID = "observation_type_id";
    public static final String DRIVER_ID = "driver_id";
    public static final String ENABLED = "enabled";
    public static final String NAME = "name";
    public static final String MIME_TYPE = "mimeType";
    public static final String DEVICE_ID = "device_id";
    public static final String DESCRIPTION = "description";
    
    public static ObservationType observationTypeFromCursor(final Cursor c, int pos) {
    	return observationTypeFromCursor(c, pos, null);
    }
    
    private static int columnWithAliasIndex(Cursor c, String alias, String colName) {
    	return c.getColumnIndexOrThrow (alias != null ? alias + "." + colName : colName);
    }
    
    public static ObservationType observationTypeFromCursor(final Cursor c, int pos, String alias) {
		c.moveToPosition(pos);
				
		final fi.hut.soberit.sensors.generic.ObservationType type = new ObservationType(
				c.getString(columnWithAliasIndex(c, alias, NAME)),
				c.getString(columnWithAliasIndex(c, alias, MIME_TYPE)),
				c.getString(columnWithAliasIndex(c, alias, DESCRIPTION)),
				null);
		
		type.setEnabled(Utils.getBooleanFromDBInt(c, alias != null? alias + "." + ENABLED : ENABLED));
		type.setId(c.getLong(columnWithAliasIndex(c, alias, OBSERVATION_TYPE_ID)));
		
		return type;
	}
}
