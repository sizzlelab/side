package fi.hut.soberit.sensors.core;

import java.util.ArrayList;
import java.util.List;

import fi.hut.soberit.sensors.Driver;
import android.database.Cursor;
import android.net.Uri;

public class DriverTable {

    public static final String AUTHORITY = "fi.hut.soberit.sensors.core";

	public static final Uri CONTENT_URI = Uri.parse("content://fi.hut.soberit.sensors.core/drivers");
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.soberit.driver";

    public static final String CONTENT_TYPES = "vnd.android.cursor.item/vnd.soberit.driver";
		
    public static final String DRIVER_ID = "driver_id";
    
    public static final String NAME = "name";
    
    public static final String URL = "url";

	public static List<Driver> multipleDriverInfoFromCursor(final Cursor c) {
		final ArrayList<Driver> drivers = new ArrayList<Driver>();
			
		for(int i=0; i<c.getCount(); i++) {
			drivers.add(driverInfoFromCursor(c, i));
		}
		
		return drivers;
	}
	
	public static Driver driverInfoFromCursor(final Cursor c, int pos) {
		c.moveToPosition(pos);
			
		final Driver driver = new Driver(
				c.getLong(c.getColumnIndex("driver_id")),
				c.getString(c.getColumnIndex("url")));
		
		return driver;
	}	
}
