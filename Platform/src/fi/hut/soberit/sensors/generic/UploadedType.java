/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.generic;

import fi.hut.soberit.sensors.Driver;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class UploadedType implements Parcelable {

	private String mimeType;
	
	private long id = -1;
	private long uploaderId;
	
	public UploadedType(String mimeType, long uploaderId) {
		this.mimeType = mimeType;
		this.uploaderId = uploaderId;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

	public long getUploaderId() {
		
		return uploaderId;
	}
	
	public void setUploaderId(long id) {
		uploaderId = id;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(mimeType);
		dest.writeLong(uploaderId);
	}
	
	public static final Parcelable.Creator<UploadedType> CREATOR
			= new Parcelable.Creator<UploadedType>() {
		
		@Override
		public UploadedType[] newArray(int size) {
		    return new UploadedType[size];
		}
		
		@Override
		public UploadedType createFromParcel(Parcel source) {
			final long id = source.readLong();
			final String mimeType = source.readString();
			final long uploaderId = source.readLong();
			
			final UploadedType type = new UploadedType(mimeType, uploaderId);
			type.setId(id);
		
			return type;
		}
	};
	
	@Override
	public String toString() {
		return String.format("%s %s %d", 
				this.getClass().getSimpleName(),
				mimeType,
				uploaderId);
	}
}
