package fi.hut.soberit.sensors.generic;

import android.os.Parcel;
import android.os.Parcelable;

public class ObservationType implements Parcelable {

	private String name;
	private String description;
	private String mimeType;
	
	private ObservationKeyname [] keynames;
	private String deviceId = "";

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public ObservationType(String name, String mimeType, String description, ObservationKeyname [] keynames) {
		this.name = name;
		this.mimeType = mimeType;
		this.description = description;
		this.keynames = keynames;
	}
	
	public ObservationType(String name, String mimeType, String description, ObservationKeyname[] keynames, String deviceId) {
		this(name, mimeType, description, keynames);
		
		this.deviceId = deviceId;
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public ObservationKeyname [] getKeynames() {
		return keynames;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(mimeType);
		dest.writeString(description);
		dest.writeString(deviceId);
		dest.writeParcelableArray(keynames, flags);
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public static final Parcelable.Creator<ObservationType> CREATOR
		= new Parcelable.Creator<ObservationType>() {
	
		@Override
	    public ObservationType[] newArray(int size) {
	        return new ObservationType[size];
	    }
	
		@Override
		public ObservationType createFromParcel(Parcel source) {
			final String name = source.readString();
			final String mimeType = source.readString();
			final String description = source.readString();
			final String deviceId = source.readString();
			
			final Parcelable[] tmp = (Parcelable[]) 
				source.readParcelableArray(ObservationKeyname.class.getClassLoader());
			
			ObservationKeyname[] keynames = ObservationKeyname.CREATOR.newArray(tmp.length);
			
			int i = 0;
			for(Parcelable p : tmp) {
				keynames[i] = (ObservationKeyname)tmp[i];
				i++;
			}
			
			return new ObservationType(name, mimeType, description, keynames, deviceId);
		}
	};
}
