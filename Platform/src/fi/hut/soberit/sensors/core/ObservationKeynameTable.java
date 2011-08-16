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
