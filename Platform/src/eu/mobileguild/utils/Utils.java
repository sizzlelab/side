/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;

public class Utils {
	
	public static final String IN = "IN";
	
	public static final String NOT_IN = "NOT IN";
	
	public static <T> List<T> copy(List<T> original) {
		ArrayList<T> copy = new ArrayList<T>(original.size());
		
		for(T obj: original) {
			copy.add(obj);
		}
		
		return copy;
	}
	
	public static Calendar calendar = Calendar.getInstance();
	
	public static long truncateDate(long date, int field) {
		switch(field) {
		case Calendar.MILLISECOND:
			return date;
		case Calendar.SECOND:
			return date - date % 1000;
		case Calendar.MINUTE:
			return date - date % (60 * 1000);
		case Calendar.HOUR:
			return date - date % (60 * 60 * 1000);
		case Calendar.DATE:
			return date - date % (24 * 60 * 60 * 1000);
		case Calendar.WEEK_OF_YEAR:
			calendar.setTime(new Date(date - date % (24 * 60 * 60 * 1000))); // round up to days.
			calendar.set(Calendar.DAY_OF_WEEK, 0);
			return date;
		}
		throw new IllegalArgumentException();
	}
	
	public static int compareTruncated(long thisDate, long thatDate, int field) {
		thisDate = truncateDate(thisDate, field);
		thatDate = truncateDate(thatDate, field);
		
		if (thisDate > thatDate) {
			return 1;
		}
		
		if (thisDate == thatDate) {
			return 0;
		}
		
		return -1;		
	}
	
	public static int getBooleanInt(SharedPreferences prefs, String key) { 
		return getBooleanInt(prefs, key, Boolean.TRUE);
	}
	
	public static int getBooleanInt(SharedPreferences prefs, String key, Boolean defValue) {
		return prefs.getBoolean(key, defValue)
			? 1
			: 0;
	}

	public static boolean getBooleanFromDBInt(Cursor c, String columnName) {
		return c.getInt(c.getColumnIndex(columnName)) == 1;
	}
	
	public static String getSetClause(String fieldName, Collection col, String in) {
		final StringBuilder builder = new StringBuilder();
		
		if (col.size() > 0) {
			builder.append(fieldName + " " + in + " (");
		}
		
		for (int i = 0; i< col.size(); i++) {
			builder.append("?, ");
		}
		
		if (col.size() > 0) {
			builder.setLength(builder.length() -2);
			builder.append(")");
		}
		
		return builder.toString();
	}

	public static String getSetClause(String fieldName, String[] arr, String in) {
		final StringBuilder builder = new StringBuilder();
		
		if (arr.length > 0) {
			builder.append(fieldName + " " + in + " (");
		}
		
		for (int i = 0; i< arr.length; i++) {
			builder.append("?, ");
		}
		
		if (arr.length > 0) {
			builder.setLength(builder.length() -2);
			builder.append(")");
		}
		
		return builder.toString();
	}
}
