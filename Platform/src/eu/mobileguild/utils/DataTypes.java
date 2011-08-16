package eu.mobileguild.utils;

import android.util.Log;

public class DataTypes {

	public static void floatToByteArray(float f, byte[] data, int start) {
		intToByteArray(Float.floatToIntBits(f), data, start);
	}

	public static float byteArrayToFloat(byte[] data, int start) {	
		return Float.intBitsToFloat(byteArrayToInt(data, start));
	}

	
	public static void intToByteArray(int num, byte[] data, int start) {
		data[start] = (byte) num;
		data[start +1] = (byte) (num >>> 8);
		data[start +2] = (byte) (num >>> 16);
		data[start +3] = (byte) (num >>> 24);
	}
	
	public static int byteArrayToInt(byte[] data, int start) {
		final int num  = (data[start +3] << 24) 
		+ ((data[start +2] & 0xFF) << 16) 
		+ ((data[start +1] & 0xFF) << 8)
		+ (data[start] & 0xFF);
		
		return num;
	}
	
	public static int readUnsignedByte(int pos, byte[] buffer) {
		final int b = buffer[pos];
		
		return (b >>> 31)*256 + b;
	}

	public static int readUnsignedWord(int pos, byte[] buffer) {
		int lowByte = readUnsignedByte(pos, buffer);
		int hiByte = readUnsignedByte(pos +1, buffer);

		return (hiByte << 8) + lowByte;
	}
	
	public static int readUnsignedWord2(int pos, byte[] buffer) {
		int lowByte = readUnsignedByte(pos, buffer);
		int hiByte = readUnsignedByte(pos +1, buffer);

		return (lowByte << 8) + hiByte;
	}
	
	public static void writeIntTo2Bytes(int value, int pos, byte[] buffer) {
		buffer[pos] 	= (byte) (value & 0xFF) ;
		buffer[pos +1]	= (byte) ((value & 0xFFFF) >> 8);
	}

	
}
