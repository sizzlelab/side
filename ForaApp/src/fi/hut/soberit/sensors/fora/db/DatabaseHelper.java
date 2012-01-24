/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.fora.db;

import eu.mobileguild.db.MGDatabaseHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper extends MGDatabaseHelper {

	private static String DATABASE_NAME = "foraLibrary.sqlite";
	private static int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, DATABASE_VERSION );
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL(TemperatureDao.TEMPERATURE_CREATE);
		db.execSQL(AmbientDao.AMBIENT_CREATE);
		db.execSQL(BloodPressureDao.PRESSURE_CREATE);
		db.execSQL(PulseDao.PULSE_CREATE);
		db.execSQL(GlucoseDao.GLUCOSE_CREATE);
		
	}
	
	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL(TemperatureDao.TEMPERATURE_DROP);
		db.execSQL(AmbientDao.AMBIENT_DROP);
		db.execSQL(BloodPressureDao.PRESSURE_DROP);
		db.execSQL(PulseDao.PULSE_DROP);
		db.execSQL(GlucoseDao.GLUCOSE_DROP);
		
		onCreate(db);
	}
}
