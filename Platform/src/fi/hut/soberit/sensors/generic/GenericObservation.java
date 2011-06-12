package fi.hut.soberit.sensors.generic;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class GenericObservation implements Parcelable, ObservationRecord {

	protected long observationTypeId;
	
	protected long time; 
	
	private byte [][] values;
	
	private static String TAG = GenericObservation.class.getSimpleName();
	
	public GenericObservation(long observationTypeId, long time, byte[][] values) {
		this.observationTypeId = observationTypeId;
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
		Log.d(TAG, "writing: " + values.length+"");
		for(byte [] value : values) {
			Log.d(TAG, "writing: " + value.length+"");
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
			final long observationTypeId = source.readLong();
			final long time = source.readLong();
			
			int size = source.readInt();
			final byte [][] values = new byte[size][];
			Log.d(TAG, values.length+"");
			for(int i = 0; i<values.length; i++) {
				size = source.readInt();
				Log.d(TAG, size +"");

				values[i] = new byte[size];
				source.readByteArray(values[i]);
			}
			
			return new GenericObservation(observationTypeId, time, values);
		}
	};

}
