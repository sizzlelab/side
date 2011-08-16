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
		Log.d(Driver.class.getSimpleName(), this + " equals  " + that
				+ " = " + (driverId == that.getId()));
		
		
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