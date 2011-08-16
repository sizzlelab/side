package fi.hut.soberit.sensors.generic;

import eu.mobileguild.utils.DataTypes;
import fi.hut.soberit.sensors.DriverInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class GenericObservation implements Parcelable {

	protected long observationTypeId;
		
	protected long time; 
	
	private byte [] values;
	
	private static String TAG = GenericObservation.class.getSimpleName();
	
	public GenericObservation(long observationTypeId, long time, byte[] values) {
		this.observationTypeId = observationTypeId;
		this.time = time;
		this.values = values;
	}

	public long getTime() {
		return time;
	}

	public byte[] getValue() {
		return values;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	public long getObservationTypeId() {
		return observationTypeId;
	}

	public void setObservationTypeId(long observationTypeId) {
		this.observationTypeId = observationTypeId;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(observationTypeId);
		dest.writeLong(time);
		
		dest.writeInt(values.length);
		dest.writeByteArray(values);
	}

	public static final Parcelable.Creator<GenericObservation> CREATOR
		= new Parcelable.Creator<GenericObservation>() {
	
		@Override
	    public GenericObservation[] newArray(int size) {
	        return new GenericObservation[size];
	    }
		
		@Override
		public GenericObservation createFromParcel(Parcel source) {
			final long observationTypeId = source.readLong();
			final long time = source.readLong();
			
			int size = source.readInt();
			final byte [] values = new byte[size];
			source.readByteArray(values);
			
			return new GenericObservation(observationTypeId, time, values);
		}
	};

	public int getValuesNum() {
		return this.values.length;
	}

	public int getInteger(int pos) {
		return DataTypes.byteArrayToInt(values, pos);
	}
	
	public float getFloat(int pos) {
		return DataTypes.byteArrayToFloat(values, pos);
	}
	
	public String toString() {
		return String.format("generic of type %d , recorded %d, has [%d]",  
				observationTypeId,
				time,
				values.length);
	}
}
