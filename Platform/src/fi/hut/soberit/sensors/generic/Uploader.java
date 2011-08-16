package fi.hut.soberit.sensors.generic;

import java.util.ArrayList;
import java.util.List;

import fi.hut.soberit.sensors.Driver;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Uploader implements Parcelable {

	private String name;
	private String url;
	private long id = -1;
	
	private boolean enabled;
	
	private UploadedType[] uploadedTypes;

	public Uploader(String name, String url) {
		super();
		this.name = name;
		this.url = url;
	}
	
	public UploadedType [] getUploadedTypes() {
		return uploadedTypes;
	}
	
	public void setUploadedTypes(UploadedType[] uploadedTypes) {
		this.uploadedTypes = uploadedTypes;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(url);
		dest.writeParcelableArray(uploadedTypes, flags);
	}
	
	public static final Parcelable.Creator<Uploader> CREATOR
			= new Parcelable.Creator<Uploader>() {
	
		@Override
	    public Uploader[] newArray(int size) {
	        return new Uploader[size];
	    }
	
		@Override
		public Uploader createFromParcel(Parcel source) {
			final long id = source.readLong();
			final String name = source.readString();
			final String url = source.readString();
			
			final Parcelable[] tmp = (Parcelable[]) 
				source.readParcelableArray(UploadedType.class.getClassLoader());
			
			UploadedType[] types = UploadedType.CREATOR.newArray(tmp.length);
			
			for(int i = 0; i< tmp.length; i++) {
				types[i] = (UploadedType) tmp[i];
			}
						
			final Uploader uploader = new Uploader(name, url);
			uploader.setId(id);
			uploader.setUploadedTypes(types);
	
			return uploader;
		}
	};

	public boolean hasAllTypes(List<ObservationType> types) {
		int counter = 0;
				
		outer:
		for (UploadedType uploaded: uploadedTypes) {
			for (ObservationType type: types) {
				if (type.getMimeType().equals(uploaded.getMimeType())) {
					counter++;
					continue outer;
				}
			}
		}
		
		return counter == uploadedTypes.length;
	}
}
