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

import java.util.WeakHashMap;

import fi.hut.soberit.sensors.DatabaseHelper;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ObservationValueProvider extends ContentProvider {

	public static final String TAG = ObservationValueProvider.class.getSimpleName();
	
	private static final int OBSERVATION_VALUES = 0;

	private static final int OBSERVATION_VALUE_ID = 1;
	
	private static UriMatcher sUriMatcher;
	
	private WeakHashMap<Long, DatabaseHelper> dbHelpers = new WeakHashMap<Long, DatabaseHelper>();
	
	@Override
	public boolean onCreate() {
		
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        
		DatabaseHelper helper;
		
        switch (sUriMatcher.match(uri)) {
        case OBSERVATION_VALUES:
        	long shardId = Long.parseLong(uri.getLastPathSegment());
        	helper = getDbHelper(shardId);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
                
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseHelper.OBSERVATION_VALUE_TABLE);
        
        Cursor c = qb.query(helper.getReadableDatabase(), 
        		projection, selection, selectionArgs, 
        		null, null, null);
        c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}

	@Override
	public String getType(Uri uri) {

		switch(sUriMatcher.match(uri)) {
		case OBSERVATION_VALUES:
			return ObservationValueTable.CONTENT_TYPES;
		case OBSERVATION_VALUE_ID:
			return ObservationValueTable.CONTENT_TYPE;
		}
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
	
        DatabaseHelper helper;
        
        switch (sUriMatcher.match(uri)) {
        case OBSERVATION_VALUES:
        	helper = getDbHelper(Long.parseLong(uri.getLastPathSegment()));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        

        
		final SQLiteDatabase db = helper.getWritableDatabase();
        
		long rowId = db.insert(DatabaseHelper.OBSERVATION_VALUE_TABLE, "", values);
        
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(ObservationValueTable.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	private DatabaseHelper getDbHelper(long sessionId) {
		DatabaseHelper helper = dbHelpers.get(sessionId);
		if (helper != null) {
			return helper;
		}
		helper = new DatabaseHelper(getContext(), sessionId); 
		
		dbHelpers.put(sessionId, helper);
		
		return helper;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        
        int count;
        switch (sUriMatcher.match(uri)) {
        case OBSERVATION_VALUES:
        {
            long shardId = Long.parseLong(uri.getLastPathSegment());
			SQLiteDatabase db = getDbHelper(shardId).getWritableDatabase();

            count = db.delete(DatabaseHelper.OBSERVATION_VALUE_TABLE, selection, selectionArgs);
            break;
        }
        case OBSERVATION_VALUE_ID:
        {
        	long shardId = Long.parseLong(uri.getPathSegments().get(2));
        	SQLiteDatabase db = getDbHelper(shardId).getWritableDatabase();
        	
            final String observationId = uri.getLastPathSegment();
            count = db.delete(DatabaseHelper.OBSERVATION_VALUE_TABLE, 
            		ObservationValueTable.OBSERVATION_VALUE_ID + "=" + observationId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                    selectionArgs);
            break;
        }
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		
        int count;
        switch (sUriMatcher.match(uri)) {
        case OBSERVATION_VALUES:
        {
        	long shardId = Long.parseLong(uri.getLastPathSegment());
			SQLiteDatabase db = getDbHelper(shardId).getWritableDatabase();

            count = db.update(DatabaseHelper.OBSERVATION_VALUE_TABLE, values, selection, selectionArgs);
            break;
        }
        case OBSERVATION_VALUE_ID:
        {
            final String valueId = uri.getPathSegments().get(1);
            final long shardId = Long.parseLong(uri.getPathSegments().get(2));
            
        	SQLiteDatabase db = getDbHelper(shardId).getWritableDatabase();

            count = db.update(DatabaseHelper.OBSERVATION_VALUE_TABLE, values, 
            		ObservationValueTable.OBSERVATION_VALUE_ID + "=" + valueId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), 
                    selectionArgs);
            break;
        }

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ObservationValueTable.AUTHORITY, "observation_values/#/", OBSERVATION_VALUES);
        sUriMatcher.addURI(ObservationValueTable.AUTHORITY, "observation_values/#/#", OBSERVATION_VALUE_ID);
    }
	
}
