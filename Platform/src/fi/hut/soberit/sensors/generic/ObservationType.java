package fi.hut.soberit.sensors.generic;
import fi.hut.soberit.sensors.Driver;
import android.os.Parcel;
import android.os.Parcelable;

public class ObservationType implements Parcelable {

	private String name;
	private long observationTypeId;
	
	private Driver driver;

	private String description;
	private String mimeType;
	
	private ObservationKeyname [] keynames;
	private boolean enabled;
	
	public ObservationType(String name, String mimeType, String description, ObservationKeyname [] keynames) {
		this.name = name;
		this.mimeType = mimeType;
		this.description = description;
		this.keynames = keynames;
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

	public void setKeynames(ObservationKeyname[] keynames) {
		this.keynames = keynames;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(observationTypeId);
		dest.writeString(name);
		dest.writeString(mimeType);
		dest.writeString(description);
		dest.writeParcelableArray(keynames, flags);
		dest.writeParcelable(driver, flags);
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public long getId() {
		return observationTypeId;
	}

	public void setId(long id) {
		this.observationTypeId = id;
	}

	public static final Parcelable.Creator<ObservationType> CREATOR
		= new Parcelable.Creator<ObservationType>() {
	
		@Override
	    public ObservationType[] newArray(int size) {
	        return new ObservationType[size];
	    }
	
		@Override
		public ObservationType createFromParcel(Parcel source) {
			final long id = source.readLong();
			final String name = source.readString();
			final String mimeType = source.readString();
			final String description = source.readString();
			
			final Parcelable[] tmp = (Parcelable[]) 
				source.readParcelableArray(ObservationKeyname.class.getClassLoader());
			
			ObservationKeyname[] keynames = ObservationKeyname.CREATOR.newArray(tmp.length);
			
			for(int i = 0; i< tmp.length; i++) {
				keynames[i] = (ObservationKeyname) tmp[i];
			}
			
			Driver driver = source.readParcelable(Driver.class.getClassLoader());
			
			final ObservationType type = new ObservationType(name, mimeType, description, keynames);
			type.setId(id);
			type.setDriver(driver);
			return type;
		}
	};

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}
	
	@Override	
	public String toString() {
		return mimeType + " " + (driver != null ? driver.getId() : "null");
		
	}
}
