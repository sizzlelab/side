package fi.hut.soberit.sensors.generic;

import android.os.Parcel;
import android.os.Parcelable;

public class ObservationKeyname implements Parcelable {
	
	private String keyname;
	
	private String unit;
	
	private String datatype;

	private long keynameId;

	private long observationTypeId;

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
		dest.writeLong(observationTypeId);
		dest.writeLong(keynameId);
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
			
			final long observationTypeId = source.readLong();
			final long keynameId = source.readLong();
			
			final ObservationKeyname obj = new ObservationKeyname(keyname, unit, datatype);
			obj.setObservationTypeId(observationTypeId);
			obj.setId(keynameId);
			return obj;
		}
	};

	public void setId(long keynameId) {
		this.keynameId = keynameId;		
	}
	
	public long getId() {
		return keynameId;
	}

	public void setObservationTypeId(long typeId) {
		this.observationTypeId = typeId;
		
	}

	public long getObservationTypeId() {
		return observationTypeId;
	}
}
