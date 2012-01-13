/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.utils;

public class LittleEndian {

	public static void writeFloat(float f, byte[] data, int start) {
		writeInt(Float.floatToIntBits(f), data, start);
	}

	public static float readFloat(byte[] data, int start) {	
		return Float.intBitsToFloat(readInt(data, start));
	}

	
	public static void writeInt(int num, byte[] data, int start) {
		data[start] = (byte) num;
		data[start +1] = (byte) (num >>> 8);
		data[start +2] = (byte) (num >>> 16);
		data[start +3] = (byte) (num >>> 24);
	}
	
	public static int readInt(byte[] data, int start) {
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
	
	public static void writeWord(int value, int pos, byte[] buffer) {
		buffer[pos] 	= (byte) (value & 0xFF) ;
		buffer[pos +1]	= (byte) ((value & 0xFFFF) >> 8);
	}	
}
