package fi.hut.soberit.sensors.fora.db;

import eu.mobileguild.db.MGDatabaseHelper;

public class AmbientDao extends TemperatureDao {

	private static final String TAG = AmbientDao.class.getSimpleName();

    public static final String AMBIENT_CREATE = 
    	"create table ambient (" +
	    	"temperature_id integer primary key, " +
	    	"temperature FLOAT, " +
	    	"time DATETIME," +
	    	"UNIQUE(time, temperature) ON CONFLICT REPLACE " +
	    	")";
	
    public static final String AMBIENT_TABLE = "ambient";

    public static final String AMBIENT_DROP = "DROP TABLE IF EXISTS ambient";
		
	public AmbientDao(MGDatabaseHelper dbHelper) {
		super(dbHelper);
		
		table = AMBIENT_TABLE;
	}
}
