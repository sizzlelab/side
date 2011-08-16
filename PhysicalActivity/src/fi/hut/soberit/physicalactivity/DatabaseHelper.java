package fi.hut.soberit.physicalactivity;

import fi.hut.soberit.fora.db.BloodPressureDao;
import fi.hut.soberit.fora.db.GlucoseDao;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper extends fi.hut.soberit.sensors.DatabaseHelper {

	public DatabaseHelper(Context context) {
		super(context);
	}
    
	public DatabaseHelper(Context context, long sessionId) {
		super(context, sessionId);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);
		
        db.execSQL(GlucoseDao.GLUCOSE_CREATE);
        db.execSQL(BloodPressureDao.PRESSURE_CREATE);
	}
	
	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onUpgrade(db, oldVersion, newVersion);
		
        db.execSQL(GlucoseDao.GLUCOSE_DROP);
        db.execSQL(BloodPressureDao.PRESSURE_DROP);
	}
}
