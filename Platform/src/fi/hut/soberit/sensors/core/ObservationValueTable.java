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

import eu.mobileguild.utils.Utils;
import fi.hut.soberit.sensors.generic.GenericObservation;
import android.database.Cursor;
import android.net.Uri;

public class ObservationValueTable {

    public static final String AUTHORITY = "fi.hut.soberit.sensors.core";

	public static final Uri CONTENT_URI = Uri.parse("content://fi.hut.soberit.sensors.core/observation_values");
		
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soberit.observation_value";

    public static final String CONTENT_TYPES = "vnd.android.cursor.item/vnd.soberit.observation_value";
	
    public static final String OBSERVATION_VALUE_ID = "observation_value_id";
    public static final String OBSERVATION_TYPE_ID = "observation_type_id";

    public static final String TIME = "time";
    public static final String VALUE = "value";

    public static GenericObservation observationFromCursor(final Cursor c, int pos) {
		c.moveToPosition(pos);
		
		final GenericObservation observation = new GenericObservation(
				c.getLong(c.getColumnIndexOrThrow(OBSERVATION_TYPE_ID)),
				c.getLong(c.getColumnIndexOrThrow(TIME)),
				c.getBlob(c.getColumnIndexOrThrow(VALUE)));
				
		return observation;
	}
}
