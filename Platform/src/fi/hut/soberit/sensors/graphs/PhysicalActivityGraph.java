/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.graphs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import eu.mobileguild.graphs.AxisFactory;
import eu.mobileguild.graphs.AxisRenderingInfo;
import eu.mobileguild.graphs.DateNamingStrategyForDoubleParameter;
import eu.mobileguild.graphs.XYChartView;
import eu.mobileguild.graphs.XYMultipleSeriesRenderer;
import eu.mobileguild.graphs.XYSeries;
import eu.mobileguild.utils.LittleEndian;
import fi.hut.soberit.sensors.DriverConnectionImpl;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.activities.BroadcastListenerActivity;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.utils.ObservationPeriod;
import fi.hut.soberit.sensors.utils.PeriodType;
import fi.hut.soberit.sensors.R;

public class PhysicalActivityGraph extends BroadcastListenerGraph {
		
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

	private static final int STROKE_WIDTH = 1;
	
	private static final int _20_SECONDS = 20 * 1000; 
		
	ObservationPeriod period;
	
	protected ObservationType pulseType;

	protected ObservationType accelerometerType;

	private XYSeries accelerometerSeries;

	private XYSeries pulseSeries;

	public PhysicalActivityGraph() {
        mainLayout = R.layout.physical_activity_graph;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        
        super.onCreate(savedInstanceState);
             
        
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
    	super.buildDriverAndUploadersTree(savedInstanceState);
    	
		for(ObservationType type: allTypes) {
			if (type.getMimeType().equals(DriverInterface.TYPE_PULSE)) {
				pulseType = type;
			} else if (type.getMimeType().equals(DriverInterface.TYPE_ACCELEROMETER)) {
				accelerometerType = type;
			}
		}
    }
	
    @Override
	protected void setupGraph() {
		period = new ObservationPeriod(new Date(), PeriodType.TWO_MINUTES);
        
		renderer.addYAxis(AxisFactory.heartBeatAxisFactory(this));
		renderer.addYAxis(AxisFactory.accelerometerAxisFactory(this));		
		renderer.addXAxis(AxisFactory.timeAxisFactory(this, period));
		
		renderer.setPaddingRight(5);
		
		renderer.setChartTitle(getString(R.string.graph_title_piece, dateFormat.format(period.getLowerBound())));		
		renderer.setLegendTextSize(18);
		renderer.setChartTitleTextSize(18);
		
		dataset = new ArrayList<XYSeries>();
		
		pulseSeries = new XYSeries(pulseType.getKeynames()[0].getKeyname(), 
				Color.RED, STROKE_WIDTH, 
				AxisFactory.HEART_BEAT_Y_MIN, AxisFactory.HEART_BEAT_Y_MAX);
		dataset.add(pulseSeries);
		
		if (accelerometerType != null) {
			accelerometerSeries = new XYSeries(getString(R.string.acceleration), 
				Color.BLUE, STROKE_WIDTH, 
				AxisFactory.ACC_Y_MIN, AxisFactory.ACC_Y_MAX);
			dataset.add(accelerometerSeries);
		}
	}

	@Override
	public void onReceiveObservations(List<Parcelable> observations) {
		Log.d(TAG, "onReceiveObservations " + observations.size());
    	
		if (observations.size() == 0) {
			return;
		}
		
		long latestObservation = period.getUpperBound().getTime() - _20_SECONDS;

		for(Parcelable parcelable : observations) {
			final GenericObservation observation = (GenericObservation) parcelable;
			
			if (accelerometerType != null && observation.getObservationTypeId() == accelerometerType.getId()) {
				
				final float x = observation.getFloat(0);
				final float y = observation.getFloat(4);
				final float z = observation.getFloat(8);
				
				accelerometerSeries.add(observation.getTime(), Math.sqrt(x*x + y*y + z*z) - SensorManager.GRAVITY_EARTH);
				
				onAccelerometerObservation(x, y, z);
			} else if (observation.getObservationTypeId() == pulseType.getId()){
				final int pulse = observation.getInteger(0);
				pulseSeries.add(observation.getTime(), pulse);
			
				onPulseObservation(pulse);
			}
			
			latestObservation = Math.max(latestObservation, observation.getTime());
		}		
		
		final AxisRenderingInfo xAxis = renderer.getXAxis(0);

		// Shift Upper bound so that it was always 20 seconds ahead of last observation
		period.shiftPeriod((latestObservation + _20_SECONDS) - period.getUpperBound().getTime());
		xAxis.setMinMax(period.getLowerBound().getTime(), period.getUpperBound().getTime());
		
		renderer.setChartTitle(getString(R.string.graph_title_piece, dateFormat.format(period.getLowerBound())));
		
		refreshGraph();
	}

	protected void onPulseObservation(int pulse) {
		
	}

	protected void onAccelerometerObservation(float x, float y, float z) {
		
	}
}
