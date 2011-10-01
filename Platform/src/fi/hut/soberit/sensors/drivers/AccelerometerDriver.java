/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.drivers;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import eu.mobileguild.utils.DataTypes;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;

public class AccelerometerDriver extends BroadcastingService implements SensorEventListener {

	protected final String TAG = this.getClass().getSimpleName();

	public final static String ACTION = AccelerometerDriver.class.getName();
	
	public static final String X_SERIES = "X";
	
	public static final String Y_SERIES = "Y";
	
	public static final String Z_SERIES = "Z";
	
	public static final String UNIT = "ms^2";
	
	public static final String SENSOR_DELAY = "sensor_delay";

	private long lastObservationRecorded = 0;
	
	private int sensorDelay;

	private long recordingFrequency;
	
	private SensorManager manager;

	protected ObservationType accelerometerType;

	public int onStartCommand(Intent intent, int flags, int startId) {		
		int res = super.onStartCommand(intent, flags, startId);
		if (intent == null) {
			return res;
		}
		
		sensorDelay = intent.getIntExtra(
				AccelerometerDriver.SENSOR_DELAY, 
				SensorManager.SENSOR_DELAY_NORMAL);
		
		Log.d(TAG, "sensor delay: " + sensorDelay);
		
		return res;
	}
	
    @Override
    public void onCreate() {
    	
		manager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

	@Override
	public void onSensorChanged(SensorEvent event) {
		final long now = System.currentTimeMillis();

		if (now - lastObservationRecorded <= recordingFrequency) {
			return;
		}
		
		lastObservationRecorded = now;
		final GenericObservation observation = accelerometerObservationFactory(now, event.values);

		addObservation(observation);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	protected GenericObservation accelerometerObservationFactory(long time, float [] values) {
		byte [] data = new byte[12];
		DataTypes.floatToByteArray(values[0], data, 0);
		DataTypes.floatToByteArray(values[1], data, 4);
		DataTypes.floatToByteArray(values[2], data, 8);
		
		return new GenericObservation(accelerometerType.getId(), time, data);		
	}
	
	@Override
	protected void onRegisterDataTypes() {
		super.onRegisterDataTypes();
		
		boolean res = manager.registerListener(
				AccelerometerDriver.this, 
				manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
				sensorDelay);
		Log.d(TAG, "registerListener result: " + res);
		
		accelerometerType = typesMap.get(DriverInterface.TYPE_ACCELEROMETER);
	}
	
	protected void onStopSession() {
		manager.unregisterListener(this);

		super.onStopSession();
	}

	@Override
    public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		manager.unregisterListener(this);
    }	
	
	@Override
	public String getDriverAction() {
		return AccelerometerDriver.ACTION;
	}
	
	public static class Discover extends BroadcastingService.Discover { 
		@Override
		public ObservationType[] getObservationTypes(Context context) {
			ObservationType [] types = new ObservationType[1];
			
			final ObservationKeyname [] keynames = new ObservationKeyname [] {
					new ObservationKeyname(AccelerometerDriver.X_SERIES, AccelerometerDriver.UNIT, DriverInterface.KEYNAME_DATATYPE_FLOAT),
					new ObservationKeyname(AccelerometerDriver.Y_SERIES, AccelerometerDriver.UNIT, DriverInterface.KEYNAME_DATATYPE_FLOAT),
					new ObservationKeyname(AccelerometerDriver.Z_SERIES, AccelerometerDriver.UNIT, DriverInterface.KEYNAME_DATATYPE_FLOAT),
			};
			
			types[0] = new ObservationType(
					"Internal accelerometer", 
					DriverInterface.TYPE_ACCELEROMETER,
					"Internal device sensor", 
					keynames);

			types[0].setId(/* temporary id */ 131030673800l);		
			
			types[0].setDriver(getDriver());
			
			return types;
		}

		public Driver getDriver() {
			final Driver driver = new Driver(AccelerometerDriver.ACTION);
			driver.setId(/* temporary id */ 131030673700l);
			return driver;
		}
	}
}
