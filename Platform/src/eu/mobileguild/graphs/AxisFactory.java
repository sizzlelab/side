/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.graphs;

import android.content.Context;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.utils.ObservationPeriod;

public class AxisFactory {
	
	public static final double HEART_BEAT_Y_MIN = 20;

	public static final double HEART_BEAT_Y_MAX = 200;

	public static final double ACC_Y_MIN = -4;

	public static final double ACC_Y_MAX = 18;

	private static final int TIME_AXIS_LABELS_NUM = 12;

	public static AxisRenderingInfo axisFactory() {
		
		AxisRenderingInfo axisInfo = new AxisRenderingInfo();
		
		axisInfo.setTitleTextSize(16);
		axisInfo.setAxisLabelTextSize(12);

		
		return axisInfo;
	}	
	
	public static AxisRenderingInfo heartBeatAxisFactory(Context context) {
		final AxisRenderingInfo axisInfo = AxisFactory.axisFactory();
		axisInfo.setTitle(context.getString(R.string.heart_beat_axis_label));
				
		axisInfo.setMinMax(
				HEART_BEAT_Y_MIN,
				HEART_BEAT_Y_MAX);
		axisInfo.setAxisLabelTextExample("200");
		axisInfo.setLabelsNum(10);
		
		return axisInfo;
	}
	
	public static AxisRenderingInfo accelerometerAxisFactory(Context context) {
		final AxisRenderingInfo axisInfo = AxisFactory.axisFactory();
		axisInfo.setTitle(context.getString(R.string.acceleration_axis_label));
				
		axisInfo.setMinMax(
				ACC_Y_MIN,
				ACC_Y_MAX);
		axisInfo.setAxisLabelTextExample("-10.0");
		
		return axisInfo;
	}
	
	public static AxisRenderingInfo timeAxisFactory(Context context, ObservationPeriod period) {
		final AxisRenderingInfo axisInfo = AxisFactory.axisFactory();
		
		axisInfo.setMinMax(
				period.getLowerBound().getTime(),
				period.getUpperBound().getTime());
		axisInfo.setTitle(context.getString(R.string.seconds));
		axisInfo.setLabelsNum(TIME_AXIS_LABELS_NUM);
		axisInfo.setNamingStrategy(new DateNamingStrategyForDoubleParameter(period.getTimeFormat()));
				
		return axisInfo;
	}
}
