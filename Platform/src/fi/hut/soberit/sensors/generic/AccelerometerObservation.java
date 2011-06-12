package fi.hut.soberit.sensors.generic;

import eu.mobileguild.utils.DataTypes;
import android.os.Parcel;
import android.os.Parcelable;

public class AccelerometerObservation extends GenericObservation {

	private float [] values;
	
	public AccelerometerObservation(long typeId, long time, float[] data) {
		super(typeId, time, null);
		
		this.values = data;
	}
	
	@Override
	public long getTime() {
		return 0;
	}

	@Override
	public byte[] getValue(int i) {
		byte [] data = new byte[4];
		
		DataTypes.floatToByteArray(values[i], data, 0);
		
		return data;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(time);
		
		byte [] data = new byte[4];

		dest.writeInt(3);
		
		DataTypes.floatToByteArray(values[0], data, 0);
		dest.writeInt(4);
		dest.writeByteArray(data);
		
		DataTypes.floatToByteArray(values[1], data, 0);
		dest.writeInt(4);
		dest.writeByteArray(data);

		DataTypes.floatToByteArray(values[2], data, 0);
		dest.writeInt(4);
		dest.writeByteArray(data);
	}
	
//	public static final Parcelable.Creator<AccelerometerObservation> CREATOR
//		= new Parcelable.Creator<AccelerometerObservation>() {
//	
//		@Override
//	    public AccelerometerObservation[] newArray(int size) {
//	        return new AccelerometerObservation[size];
//	    }
//	
//		@Override
//		public AccelerometerObservation createFromParcel(Parcel source) {
//			final long time = source.readLong();
//			final byte [] data = new byte[4];
//			
//			source.readInt(); // always 3
//			final float [] values = new float[3];
//			
//			source.readInt(); // always 4
//			source.readByteArray(data);
//			values[0] = DataTypes.byteArrayToFloat(data, 0);
//
//			source.readInt(); // always 4
//			source.readByteArray(data);
//			values[1] = DataTypes.byteArrayToFloat(data, 0);
//			
//			source.readInt(); // always 4
//			source.readByteArray(data);
//			values[2] = DataTypes.byteArrayToFloat(data, 0);
//			
//			return new AccelerometerObservation(time, values);
//		}
//	};

}
