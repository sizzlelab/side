package fi.hut.soberit.sensors.generic;

import android.os.Parcel;
import android.os.Parcelable;

public class ObservationKeyname implements Parcelable {
		
	private String keyname;
	
	private String unit;
	
	private String datatype;

	public ObservationKeyname(String keyname, String unit, String datatype) {
		this.keyname = keyname;
		this.unit = unit;
		this.datatype = datatype;			
	}

	public String getKeyname() {
		return keyname;
	}

	public String getUnit() {
		return unit;
	}

	public String getDatatype() {
		return datatype;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(keyname);
		dest.writeString(unit);
		dest.writeString(datatype);
	}
	
	public static final Parcelable.Creator<ObservationKeyname> CREATOR
		= new Parcelable.Creator<ObservationKeyname>() {
	
		@Override
	    public ObservationKeyname[] newArray(int size) {
	        return new ObservationKeyname[size];
	    }
	
		@Override
		public ObservationKeyname createFromParcel(Parcel source) {
			final String keyname = source.readString();
			final String unit = source.readString();
			final String datatype = source.readString();
			
			return new ObservationKeyname(keyname, unit, datatype);
		}
	};
}
