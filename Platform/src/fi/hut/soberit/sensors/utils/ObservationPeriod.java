/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.utils;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import eu.mobileguild.utils.Debug;

import android.util.Log;


public class ObservationPeriod {
	private static final String TAG = ObservationPeriod.class.getSimpleName();
	Date lowerBound;
	PeriodType boundGauge;

	public ObservationPeriod(Date lowerBound, PeriodType boundGauge) {
		this.lowerBound = lowerBound;
		this.boundGauge = boundGauge;
	}	
	
	public Date getLowerBound() {
		return lowerBound;
	}
	
	public Date getUpperBound() {
		return DateUtils.addMilliseconds(lowerBound, boundGauge.getMilliseconds());
	}
	
	public static Date shiftDateTimes(Date date, PeriodType boundGauge, int directionAndTimes) {
		for(int i=0; i<Math.abs(directionAndTimes); i++) {
			date = directionAndTimes > 0
				? DateUtils.addMilliseconds(date, boundGauge.getMilliseconds())
				: DateUtils.addMilliseconds(date, -boundGauge.getMilliseconds());			
		}
			
		return date;
	}
	
	public int getMeasureId() {
		return boundGauge.getMeasureId();
	}

	public PeriodType getBoundGauge() {
		return boundGauge;
	}

	public String getTimeFormat() {
		return boundGauge.getTimeFormat();	
	}


	public void narrowGauge() {	
		final Date oldUpperBound = getUpperBound();

		boundGauge = boundGauge.getMoreNarrowPeriodType();
		lowerBound.setTime(oldUpperBound.getTime() - boundGauge.getMilliseconds());
		
		Log.d(TAG, "Gauge made more narrow. New gauge: " + boundGauge.name());
	}

	public void widerGauge() {
		final Date oldUpperBound = getUpperBound();

		boundGauge = boundGauge.getWiderPeriodType();
		lowerBound.setTime(oldUpperBound.getTime() - boundGauge.getMilliseconds());

		Log.d(TAG, "Gauge made wider. New gauge: " + boundGauge.name());
	}

	public void shiftPeriod(long milliseconds) {
		lowerBound.setTime(lowerBound.getTime() + milliseconds);

		Log.d(TAG, "Bound shifted. New bound: " + Debug.DATE_FORMAT.format(lowerBound));
	}
	
	
	public static ObservationPeriod createShiftedPeriod(ObservationPeriod period, int milliseconds) {
		return new ObservationPeriod(
				DateUtils.addMilliseconds(period.lowerBound, milliseconds),
				period.boundGauge);
	}

	public void setBondGauge(PeriodType boundGauge) {
		this.boundGauge = boundGauge;		
	}

	public static ObservationPeriod copy(ObservationPeriod period) {
		return new ObservationPeriod(period.lowerBound, period.boundGauge);
	}
	
	public int relativePosition(ObservationPeriod otherPeriod) {
		if (lowerBound.compareTo(otherPeriod.lowerBound) > 0) {
			return -1;
		}
		
		if (getUpperBound().compareTo(otherPeriod.getUpperBound()) < 0 ) {
			return 1;
		}
		return 0;
	}

	public int relativePosition(Date time) {
		if (lowerBound.compareTo(time) > 0) {
			return -1;
		}
		
		if (getUpperBound().compareTo(time) < 0) {
			return 1;
		}
		return 0;
	}
	
	public String toString() {
		return "" 
			+ boundGauge.name()
			+ " "
			+ lowerBound.toString()
			+ " - "
			+ getUpperBound().toString();
	}
}

