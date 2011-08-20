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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import eu.mobileguild.graphs.XYChartView;
import eu.mobileguild.graphs.XYMultipleSeriesRenderer;
import eu.mobileguild.graphs.XYSeries;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.activities.BroadcastListenerActivity;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.utils.ObservationPeriod;

public abstract class BroadcastListenerGraph extends BroadcastListenerActivity {

	public final String TAG = this.getClass().getSimpleName(); 
	
	private final static String SIS_TYPES = "sis types";

	protected int mainLayout;
	
	protected XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	
	private LinearLayout graphParentLayout;
	
	protected ObservationPeriod period;

	protected XYChartView chartView;
	
	protected ArrayList<XYSeries> dataset;

	protected Handler uiCallback;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        
    	super.onCreate(savedInstanceState);
        setContentView(mainLayout);       
             
		setupGraph();
		
		chartView = new XYChartView(this, renderer, dataset);

		graphParentLayout = (LinearLayout) findViewById(R.id.graph_container);
		graphParentLayout.addView(chartView, new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		uiCallback = new UIHandler();		
    }

    @Override
	protected void buildDriverAndUploadersTree(Bundle savedInstanceState) {
		ArrayList<Parcelable> parcelableTypes = null;
		
		final Intent startIntent = getIntent();
		
		if (startIntent != null) {
			Bundle payload = startIntent.getExtras();
			payload.setClassLoader(getClassLoader());
			
			parcelableTypes = payload.getParcelableArrayList(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES);
		} else {
			savedInstanceState.setClassLoader(getClassLoader());
			
			parcelableTypes = savedInstanceState.getParcelableArrayList(SIS_TYPES);
		}
		
		allTypes = new ArrayList<ObservationType>();
		driverTypes = new HashMap<Driver, ArrayList<ObservationType>>();
		for(Parcelable parcelable: parcelableTypes) {
			final ObservationType type = (ObservationType) parcelable;
			allTypes.add(type);
			
			final Driver driver = type.getDriver();
			type.setDriver(driver);
			
			ArrayList<ObservationType> types = new ArrayList<ObservationType>();
			
			if (types == null) {
				types = new ArrayList<ObservationType>();
				driverTypes.put(driver, types);
			}
			
			types.add(type);
		}
    }

	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		
		state.putParcelableArrayList(SIS_TYPES, allTypes);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	protected abstract void setupGraph();

	protected void refreshGraph() {
		uiCallback.sendEmptyMessage(0);		
	}

	@Override
	public abstract void onReceiveObservations(List<Parcelable> observations);
	
	class UIHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "handleMessage");

			chartView.invalidate();
		}
	}
}
