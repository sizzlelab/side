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

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import fi.hut.soberit.sensors.generic.ObservationType;

public class Driver implements Comparable<Driver>, Parcelable {
	
	private long driverId;
	
	private String url;
	
	public Driver(long driverId, String url) {
		this.driverId = driverId;
		this.url = url;
	}	

	public Driver(String url) {
		this.url = url;
	}	
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getId() {
		return driverId;
	}
	
	public void setId(long id) {
		this.driverId = id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Driver)) {
			return false;
		}
		
		final Driver that = (Driver)o;
		
		return driverId == that.getId();
	}

	public int compareTo(Driver object2) {
		long id2 = object2.getId();
		
		if (driverId < id2) {
			return -1;
		}
		
		if (driverId > id2) {
			return 1;
		}
		
		return 0;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(url);
		dest.writeLong(driverId);
	}
	
	public int hashCode() {
		return (int) driverId;
	}
	
	public String toString() {
		return driverId + ": " + url;
	}
	
	public static final Parcelable.Creator<Driver> CREATOR
		= new Parcelable.Creator<Driver>() {
	
		@Override
	    public Driver[] newArray(int size) {
	        return new Driver[size];
	    }
	
		@Override
		public Driver createFromParcel(Parcel source) {
			
			final String url = source.readString();
			final Long id = source.readLong();
			
			return new Driver(id, url);
		}
	};
}
