/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * Maksim Golivkin <maksim@golivkin.eu>
 ******************************************************************************/
package fi.hut.soberit.accelerometer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import eu.mobileguild.graphs.AxisFactory;
import eu.mobileguild.graphs.AxisRenderingInfo;
import eu.mobileguild.graphs.DateNamingStrategyForDoubleParameter;
import eu.mobileguild.graphs.XYChartView;
import eu.mobileguild.graphs.XYMultipleSeriesRenderer;
import eu.mobileguild.graphs.XYSeries;
import eu.mobileguild.utils.DataTypes;
import fi.hut.soberit.sensors.DriverConnection;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.utils.ObservationPeriod;
import fi.hut.soberit.sensors.utils.PeriodType;
import fi.hut.soberite.accelerometer.R;

public class AccelerometerGraph extends Activity {

	public final static String TAG = AccelerometerGraph.class.getSimpleName(); 
	
	private final static String SIS_TYPES = "sis types";
	
	XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

	private static final int STROKE_WIDTH = 0;

	private static final double Y_MAX = -18;

	private static final double Y_MIN = 18;
	
	private LinearLayout graphParentLayout;
	
	{
		renderer.setLegendTextSize(18);
		renderer.setChartTitleTextSize(18);
	}
	
	ObservationPeriod period;

	private XYChartView chartView;
	
	final ArrayList<DriverConnection> connections = new ArrayList<DriverConnection>();

	private ArrayList<XYSeries> dataset;

	Handler uiCallback;
	
	HashMap<String, ObservationType> types = new HashMap<String, ObservationType>();

	public static final double ACCELEROMETER_Y_MAX = 18;
	public static final double ACCELEROMETER_Y_MIN = -18;
	public static final int X_AXIS_LABELS_NUM = 12;
	
	private int[] colors = new int [] {
		Color.BLUE,
		Color.GREEN,
		Color.RED
	};

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);       
             	
		initAndConnect(getIntent(), savedInstanceState);
        
		period = new ObservationPeriod(new Date(), PeriodType.TWO_MINUTES);
        
		final AxisRenderingInfo yLeftAxis = AxisFactory.axisFactory();
		yLeftAxis.setTitle(getString(R.string.accelerometer_title));
				
		yLeftAxis.setMinMax(
				ACCELEROMETER_Y_MIN,
				ACCELEROMETER_Y_MAX);
		yLeftAxis.setAxisLabelTextExample("-10.0");
		yLeftAxis.setAxisLabelTextSize(12);
		
		renderer.addYAxis(yLeftAxis);
		
		final AxisRenderingInfo xAxis = AxisFactory.axisFactory();
		
		xAxis.setMinMax(
				period.getLowerBound().getTime(),
				period.getUpperBound().getTime());
		xAxis.setTitle(getString(R.string.seconds));
		xAxis.setLabelsNum(X_AXIS_LABELS_NUM);
		xAxis.setNamingStrategy(new DateNamingStrategyForDoubleParameter(period.getTimeFormat()));
		
		renderer.addXAxis(xAxis);
		renderer.setPaddingRight(25);
		
		renderer.setChartTitle(getString(R.string.graph_title_piece, dateFormat.format(period.getLowerBound())));		
		
		dataset = new ArrayList<XYSeries>();
		
		final ObservationKeyname[] keynames = types.get(DriverInterface.TYPE_ACCELEROMETER).getKeynames();
		dataset.add(new XYSeries(keynames[0].getKeyname(), colors[0], STROKE_WIDTH, Y_MIN, Y_MAX));
		dataset.add(new XYSeries(keynames[1].getKeyname(), colors[1], STROKE_WIDTH, Y_MIN, Y_MAX));
		dataset.add(new XYSeries(keynames[2].getKeyname(), colors[2], STROKE_WIDTH, Y_MIN, Y_MAX));
		
		chartView = new XYChartView(this, renderer, dataset);

		graphParentLayout = (LinearLayout) findViewById(R.id.graph);
		graphParentLayout.addView(chartView, new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		
		uiCallback = new UIHandler();
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

	private void initAndConnect(Intent startIntent, Bundle savedInstanceState) {
			
		ArrayList<Parcelable> parcelableTypes = null;
		
		if (startIntent != null) {
			Bundle payload = startIntent.getExtras();
			payload.setClassLoader(getClassLoader());
			
			parcelableTypes = payload.getParcelableArrayList(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES);
		} else {
			savedInstanceState.setClassLoader(getClassLoader());
			
			parcelableTypes = savedInstanceState.getParcelableArrayList(SIS_TYPES);
		}

			
		HashMap<Driver, ArrayList<ObservationType>> drivers = new HashMap<Driver, ArrayList<ObservationType>>();
	
		
		for(Parcelable parcelable: parcelableTypes) {
			final ObservationType type = (ObservationType) parcelable;

			if (!isMimeTypeInteresting(type.getMimeType())) {
				continue;
			}

			this.types.put(type.getMimeType(), type);			
			
			ArrayList<ObservationType> driverTypes = drivers.get(type.getDriver());
			
			if (driverTypes == null) {
				driverTypes = new ArrayList<ObservationType>();
				drivers.put(type.getDriver(), driverTypes);
			}
			
			driverTypes.add(type);
		}
		
		for (Driver driver : drivers.keySet()) {			
			final DriverConnectionImpl driverConnection = new DriverConnectionImpl(driver, drivers.get(driver));
			connections.add(driverConnection);	
			
			driverConnection.bind(this);
		}
	}

	private boolean isMimeTypeInteresting(String mimeType) {
		return DriverInterface.TYPE_ACCELEROMETER.equals(mimeType);
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		
		state.putParcelableArrayList(SIS_TYPES, new ArrayList(types.values()));
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		
		for(DriverConnection connection : connections) {
			if (!connection.isServiceConnected()) {
				continue;
			}
			
			connection.unregisterClient();
			unbindService(connection);
						
		}
	}
	
//	private ObservationKeyname[] getObservationKeynames(ObservationType type) {
//		final String selection = String.format("%s = ?", fi.hut.soberit.sensors.core.ObservationKeyname.OBSERVATION_TYPE_ID);
//		final String[] selectionArgs = new String[] {type.getId() + ""};
//
//		final Cursor c = provider.query(fi.hut.soberit.sensors.core.ObservationKeyname.CONTENT_URI, 
//				null, selection, selectionArgs, "keyname_id ASC");
//		
//		
//		return null;
//	}
//
//	private List<ObservationType> getObservationTypes(DriverInfo driver) {	
//		final String selection = String.format("%s = ?", fi.hut.soberit.sensors.core.ObservationType.DRIVER_ID);
//		
//		final String[] selectionArgs = new String[] {driver.getId() + ""};
//		
//		final Cursor c = provider.query(fi.hut.soberit.sensors.core.ObservationType.CONTENT_URI, 
//				null, selection, selectionArgs, null);
//		
//		List<ObservationType> types = new ArrayList<ObservationType>();
//		
//		for (int i = 0; i<c.getCount(); i++) {
//			types.add(fi.hut.soberit.sensors.core.ObservationType.observationTypeFromCursor(c, i));
//		}
//		
//		return types;
//	}
//
//	private List<DriverInfo> getDrivers(final String[] drivers) {
//
//		final String selection = Utils.getInClause(Driver.URL, drivers);
//		
//		final Cursor c = provider.query(Driver.CONTENT_URI, null, selection, drivers, null);
//		
//		List<DriverInfo> driverObjects = Driver.multipleDriverInfoFromCursor(c);
//		return driverObjects;
//	}
	
	class DriverConnectionImpl extends DriverConnection {

		public DriverConnectionImpl(Driver driver, List<ObservationType> types) {
			super(driver, types, false);

		}

		@Override
		public void onReceiveObservations(List<Parcelable> observations) {
			Log.d(TAG, "onReceiveObservations");
			
			if (observations.size() == 0) {
				Log.d(TAG, "empty observations");

				return;
			}
			Log.d(TAG, "not empty observations");

			final GenericObservation theLatest = (GenericObservation) observations.get(0);
			
			final XYSeries xSeries = (XYSeries) dataset.get(0);
			int pos = 0;
			float x = theLatest.getFloat(pos );
			pos += 4;
			Log.d(TAG, "x = " + x);
			
			xSeries.add(theLatest.getTime(), x);
			
			final XYSeries ySeries = (XYSeries) dataset.get(1);
			ySeries.add(theLatest.getTime(), theLatest.getFloat(pos));
			pos += 4;
			
			final XYSeries zSeries = (XYSeries) dataset.get(2);
			zSeries.add(theLatest.getTime(), theLatest.getFloat(pos));
			
			AccelerometerGraph.this.uiCallback.sendEmptyMessage(0);
		}
	}
	
	class UIHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "handleMessage");

			chartView.invalidate();
			
		}
	}
}
