package com.liiqu.db;

import com.liiqu.event.EventDao;
import com.liiqu.response.ResponseDao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import eu.mobileguild.db.MGDatabaseHelper;

public class DatabaseHelper extends MGDatabaseHelper {

	private static final String DATABASE_FILE = "liiqu.sqlite";
	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_FILE, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(EventDao.EVENT_CREATE);
		db.execSQL(ResponseDao.RESPONSE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL(EventDao.EVENT_DROP);
		db.execSQL(ResponseDao.RESPONSE_DROP);

		db.execSQL(EventDao.EVENT_CREATE);
		db.execSQL(ResponseDao.RESPONSE_CREATE);
	}
}
