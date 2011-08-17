/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.generic;

import java.util.ArrayList;
import java.util.List;

import fi.hut.soberit.sensors.Driver;

import android.os.Parcel;
import android.os.Parcelable;

public class Storage implements Parcelable {

	private long id;
	private ArrayList<ObservationType> types;
	
	private String url;

	public Storage(long id, String url) {
		this.id = id;
		this.url = url;
	}

	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ArrayList<ObservationType> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<ObservationType> types) {
		this.types = types;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeTypedList(types);
		dest.writeString(url);
	}
	
	public static final Parcelable.Creator<Storage> CREATOR
		= new Parcelable.Creator<Storage>() {
	
		@Override
	    public Storage[] newArray(int size) {
	        return new Storage[size];
	    }
	
		@Override
		public Storage createFromParcel(Parcel source) {
			final long id = source.readLong();
	
			ArrayList<ObservationType> types = new ArrayList<ObservationType>();
			source.readTypedList(types, ObservationType.CREATOR);
			
			final String url = source.readString();
			
			final Storage storage = new Storage(id, url);
			storage.setTypes(types);
			return storage;
		}
	};
	
	public int hashCode() {
		return (int)id;
	}
}
