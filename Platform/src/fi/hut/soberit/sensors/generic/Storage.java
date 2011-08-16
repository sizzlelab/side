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
