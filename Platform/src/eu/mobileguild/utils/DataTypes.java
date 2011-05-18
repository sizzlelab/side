package eu.mobileguild.utils;

public class DataTypes {

	public static void floatToByteArray(float f, byte[] data, int start) {
		final int intBits = Float.floatToIntBits(f);
		data[start] = (byte) intBits;
		data[start +1] = (byte) (intBits >>> 8);
		data[start +2] = (byte) (intBits >>> 16);
		data[start +3] = (byte) (intBits >>> 24);
	}

	public static float byteArrayToFloat(byte[] data, int start) {
		return (data[start +3] << 24) 
			+ ((data[start +2] & 0xFF) << 16) 
			+ ((data[start +1] & 0xFF) << 8)
			+ (data[start] & 0xFF);
	}

}
