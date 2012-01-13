/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors;
import eu.mobileguild.db.MGDatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class DatabaseHelper extends MGDatabaseHelper {

    public static final String DATABASE_NAME = "sessions.sqlite";
    private static final String SHARD_NAME = "shard";    
    
    public static final int DATABASE_VERSION = 5;

    static final String OBSERVATION_TYPE_CREATE = 
    	"create table observation_type (" +
	    	"observation_type_id integer primary key, " +
	    	"driver_id integer, " +
	    	"enabled INTEGER, " +
	    	"name TEXT, " +
	    	"mimeType TEXT, " +
	    	"description TEXT )";

    static final String OBSERVATION_TYPE_INDEX_CREATE = 
    	"CREATE UNIQUE INDEX i_observation_type_driver_mimeType " +
    	"ON observation_type(driver_id, mimeType);";
    
    static final String OBSERVATION_KEYNAME_CREATE = 
    	"create table observation_keyname (" +
	    	"observation_keyname_id integer primary key, " +
	    	"observation_type_id INTEGER, " +
	    	"keyname TEXT, " +
	    	"unit TEXT, " +
	    	"datatype TEXT)";

    static final String OBSERVATION_KEYNAME_INDEX_CREATE = 
    	"CREATE UNIQUE INDEX i_observation_keyname_type_keyname " +
    	"ON observation_keyname(observation_type_id, keyname);";
    
    static final String OBSERVATION_VALUE_CREATE = 
    	"create table observation_value (" +
    		"observation_value_id INTEGER primary key, " +
    		"observation_type_id INTEGER," +
    		"time INTEGER(8), " +
    		"value BLOB," +
    		"UNIQUE(time, observation_type_id) ON CONFLICT REPLACE" +
    		")";
    
    static final String OBSERVATION_VALUE_INDEX_CREATE = 
    	"CREATE UNIQUE INDEX i_observation_value_time_type_keyname " +
    	"ON observation_value(time, observation_type_id);";
    
    static final String DRIVER_CREATE = 
    	"create table driver (" +
    		"driver_id INTEGER primary key, " +
    		"url TEXT)";
    
    static final String UPLOADER_CREATE = 
    	"create table uploader (" +
    		"uploader_id INTEGER primary key, " +
    		"name TEXT," +
    		"enabled INTEGER," +
    		"url TEXT)";    

    static final String UPLOADED_TYPE_CREATE = 
    	"create table uploaded_type (" +
    		"uploaded_type_id INTEGER primary key, " +
    		"mime_type TEXT, " +
    		"uploader_id INTEGER)";    
    
    static final String STORAGE_CREATE = 
    	"create table storage (" +
    		"storage_id INTEGER primary key, " +
    		"url TEXT)";    
    
    static final String STORAGE_TYPE_CREATE = 
    	"create table storage_type (" +
    		"storage_id INTEGER, " +
    		"observation_type_id INTEGER)";    
    
    static final String OBSERVATION_TYPE_DROP = 
    	"DROP TABLE IF EXISTS observation_type";

    static final String OBSERVATION_TYPE_INDEX_DROP = 
    	"DROP INDEX IF EXISTS i_observation_type_driver_mimeType";
    
    static final String OBSERVATION_KEYNAME_DROP = 
    	"DROP TABLE IF EXISTS observation_keyname";

    static final String OBSERVATION_KEYNAME_INDEX_DROP = 
    	"DROP INDEX IF EXISTS i_observation_keyname_type_keyname";
    
    static final String OBSERVATION_VALUE_DROP = 
    	"DROP TABLE IF EXISTS observation_value";
    
    static final String OBSERVATION_VALUE_INDEX_DROP = 
    	"DROP INDEX IF EXISTS i_observation_value_time_type_keyname";

    static final String DRIVER_DROP = "DROP TABLE IF EXISTS driver";
    
    static final String UPLOADER_DROP = 
    	"DROP INDEX IF EXISTS uploader";
    
    static final String UPLOADED_TYPE_DROP = 
    	"DROP INDEX IF EXISTS uploaded_type";
    
    static final String STORAGE_DROP =
    	"DROP TABLE IF EXISTS storage";

    static final String STORAGE_TYPE_DROP =
    	"DROP TABLE IF EXISTS storage_type";
    

    public static final String OBSERVATION_TYPE_TABLE = "observation_type";
    
    public static final String OBSERVATION_KEYNAME_TABLE = "observation_keyname";

    public static final String OBSERVATION_VALUE_TABLE = "observation_value";
    
    public static final String DRIVER_TABLE = "driver";
    
    public static final String UPLOADER_TABLE = "uploader";
    
    public static final String UPLOADED_TYPE_TABLE = "uploaded_type";
    
    public static final String STORAGE_TABLE = "storage";

    public static final String STORAGE_TYPE_TABLE = "storage_type";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, long sessionId) {
        super(context, shardName(sessionId), DATABASE_VERSION);
        Log.d(TAG, "Opening shard " + sessionId);
    }

    public static String shardName(long sessionId) {
		return SHARD_NAME + sessionId + ".sqlite";
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(OBSERVATION_TYPE_CREATE);
        db.execSQL(OBSERVATION_TYPE_INDEX_CREATE);
        db.execSQL(OBSERVATION_KEYNAME_CREATE);
        db.execSQL(OBSERVATION_KEYNAME_INDEX_CREATE);
        db.execSQL(OBSERVATION_VALUE_CREATE);
        db.execSQL(OBSERVATION_VALUE_INDEX_CREATE);
        db.execSQL(SessionDao.SESSION_CREATE);
        db.execSQL(DRIVER_CREATE);
        db.execSQL(UPLOADER_CREATE);
        db.execSQL(UPLOADED_TYPE_CREATE);
        db.execSQL(STORAGE_CREATE);
        db.execSQL(STORAGE_TYPE_CREATE);
	}
	
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        
        db.execSQL(OBSERVATION_TYPE_DROP);
        db.execSQL(OBSERVATION_TYPE_INDEX_DROP);
        db.execSQL(OBSERVATION_KEYNAME_DROP);
        db.execSQL(OBSERVATION_KEYNAME_INDEX_DROP);
        db.execSQL(OBSERVATION_VALUE_DROP);
        db.execSQL(OBSERVATION_VALUE_INDEX_DROP);
        db.execSQL(SessionDao.SESSION_DROP);
        db.execSQL(DRIVER_DROP);
        db.execSQL(UPLOADER_DROP);
        db.execSQL(UPLOADED_TYPE_DROP);
        db.execSQL(STORAGE_CREATE);
        db.execSQL(STORAGE_TYPE_CREATE);
        
        onCreate(db);
    }	
}
