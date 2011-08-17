/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.core.storage;

import android.content.ContentValues;
import android.database.Cursor;
import fi.hut.soberit.sensors.generic.Storage;

public class StorageTable {

	public static final String ID = "storage_id";
	public static final String URL = "url";

	public static Storage storageFromCursor(Cursor c, int i) {
		c.moveToPosition(i);
		
		Storage storage = new Storage(
				c.getLong(c.getColumnIndexOrThrow(ID)),
				c.getString(c.getColumnIndexOrThrow(URL)));
		
		return storage;
	}
		
	public static ContentValues valuesFromStorage(Storage storage) {
		
		ContentValues values = new ContentValues(); 
		values.put(ID, storage.getId());
		values.put(URL, storage.getUrl());
		
		return values;
	}
}
