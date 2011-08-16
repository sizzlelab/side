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
