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
