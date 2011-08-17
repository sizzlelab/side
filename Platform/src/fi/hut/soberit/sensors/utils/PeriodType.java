/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import fi.hut.soberit.sensors.R;

import android.os.Parcel;
import android.os.Parcelable;

public enum PeriodType implements Parcelable {
	TWO_SECONDS(2, 1000, R.string.second, "s"),
	TWENTY_SECONDS(20, 1000, R.string.second, "s"),
	TWO_MINUTES(2 * 60, 1000, R.string.second, "s"),
	TWO_HOURS(2 * 60, 60 * 1000, R.string.minute, "m"),
	ONE_DAY(24, 60 * 60 * 1000, R.string.hour, "H"),
	ONE_WEEK(7, 24 * 60 * 60 * 1000, R.string.weekday, "E");
	
	private final int units;
	private final int measureId;
	private final int unitVolume;
	private String timeFormat;
	
	private static Map<PeriodType, Integer> gaugePrecisionMap = new HashMap<PeriodType, Integer>();
	
	static {
		gaugePrecisionMap.put(PeriodType.TWO_SECONDS, Calendar.MILLISECOND);
		gaugePrecisionMap.put(PeriodType.TWENTY_SECONDS, Calendar.MILLISECOND);
		gaugePrecisionMap.put(PeriodType.TWO_MINUTES, Calendar.MILLISECOND);
		gaugePrecisionMap.put(PeriodType.TWO_HOURS, Calendar.MINUTE);
		gaugePrecisionMap.put(PeriodType.ONE_DAY, Calendar.HOUR);
		gaugePrecisionMap.put(PeriodType.ONE_WEEK, Calendar.DATE);
	}
	
	PeriodType(int units, int unitVolume, int measureId, String timeFormat) {
		this.units = units;
		
		this.measureId = measureId;
		this.unitVolume = unitVolume;
		this.timeFormat = timeFormat;
	}

	public int getMilliseconds() {
		return units * unitVolume;
	}
	
	public int getUnitVolume() {
		return unitVolume;
	}

	public int getUnits() {
		return units;
	}

	public int getMeasureId() {
		return measureId;
	}
	
	public String getTimeFormat() {
		return timeFormat;
	}

	public boolean isAggregated() {
		return ordinal() >= TWO_HOURS.ordinal();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ordinal());
	}
	
	public static final Parcelable.Creator<PeriodType> CREATOR
			= new Parcelable.Creator<PeriodType>() {
		
		@Override
		public PeriodType[] newArray(int size) {
		    return new PeriodType[size];
		}
		
		@Override
		public PeriodType createFromParcel(Parcel source) {
			return PeriodType.values()[source.readInt()];
		}
	};
	
	public boolean isThereWiderPeriodType() {
		return ordinal() < PeriodType.values().length - 1;
	}
	
	public PeriodType getWiderPeriodType() {
		return PeriodType.values()[this.ordinal() +1];
	}

	public boolean isThereMoreNarrowPeriodType() {
		return ordinal() > 0;
	}
	
	public PeriodType getMoreNarrowPeriodType() {
		return PeriodType.values()[this.ordinal() -1];
	}
	
	public int getGaugePrecision() {
		return gaugePrecisionMap.get(this);
	}
}
