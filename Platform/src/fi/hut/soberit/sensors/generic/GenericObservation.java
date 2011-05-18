package fi.hut.soberit.sensors.generic;

import android.os.Parcel;
import android.os.Parcelable;

public class GenericObservation implements Parcelable, ObservationRecord {

	protected long time; 
	
	private byte [][] values;
	
	public GenericObservation(long time, byte[][] values) {
		this.time = time;
		this.values = values;
	}

	@Override
	public long getTime() {
		return time;
	}

	@Override
	public byte[] getValue(int i) {
		return values[i];
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(time);
		
		dest.writeInt(values.length);
		for(byte [] value : values) {
			dest.writeInt(value.length);
			dest.writeByteArray(value);
		}
	}

	public static final Parcelable.Creator<GenericObservation> CREATOR
		= new Parcelable.Creator<GenericObservation>() {
	
		@Override
	    public GenericObservation[] newArray(int size) {
	        return new GenericObservation[size];
	    }
	
		@Override
		public GenericObservation createFromParcel(Parcel source) {
			final long time = source.readLong();
			
			final byte [][] values = new byte[source.readInt()][];
			
			for(int i = 0; i<values.length; i++) {
				values[i] = new byte[source.readInt()];
				source.readByteArray(values[i]);
			}
			
			return new GenericObservation(time, values);
		}
	};

}
