/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.core;

import android.database.Cursor;
import android.net.Uri;

public class ObservationKeynameTable {

    public static final String AUTHORITY = "fi.hut.soberit.sensors.core";

	public static final Uri CONTENT_URI = Uri.parse("content://fi.hut.soberit.sensors.core/observation_keynames");
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soberit.observation_keyname";

    public static final String CONTENT_TYPES = "vnd.android.cursor.item/vnd.soberit.observation_keyname";
	
    public static final String OBSERVATION_KEYNAME_ID = "observation_keyname_id";
    public static final String OBSERVATION_TYPE_ID = "observation_type_id";
    public static final String KEYNAME = "keyname";
    public static final String UNIT = "unit";
    public static final String DATATYPE = "datatype";
    
	
	public static fi.hut.soberit.sensors.generic.ObservationKeyname observationToKeyname(Cursor c, int pos) {
		c.moveToPosition(pos);
		
		final fi.hut.soberit.sensors.generic.ObservationKeyname keyname = new fi.hut.soberit.sensors.generic.ObservationKeyname(
			c.getString(c.getColumnIndex("keyname")),
			c.getString(c.getColumnIndex("unit")),
			c.getString(c.getColumnIndex("datatype"))
		);
		
		keyname.setId(c.getLong(c.getColumnIndex("observation_keyname_id")));
		keyname.setObservationTypeId(c.getLong(c.getColumnIndex("observation_type_id")));		
		
		return keyname;
	}
}
