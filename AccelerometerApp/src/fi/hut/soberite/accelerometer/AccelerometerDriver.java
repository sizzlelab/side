package fi.hut.soberite.accelerometer;

import java.util.ArrayList;
import java.util.List;

import eu.mobileguild.utils.DataTypes;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.AccelerometerObservation;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationRecord;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class AccelerometerDriver extends Service implements SensorEventListener {

    private static final String TAG = AccelerometerDriver.class.getSimpleName();

	public static final String APP_PREFERENCES_FILE = "accDriver.settings";

	ArrayList<Messenger> clients = new ArrayList<Messenger>();

	ArrayList<GenericObservation> observations = new ArrayList<GenericObservation>();
	
	public long accelerometerDataType = 0;

	private long lastObservationBroadcasted = 0 ;
    
	private long lastObservationRecorded = 0;
	
    final Messenger mMessenger = new Messenger(new IncomingHandler());

	private int sensorDelay;

	private long recordingFrequency;

	private long broadcastFrequency;

	SensorManager manager;

	public AccelerometerDriver() {

	}

	@Override
    public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		
		final SharedPreferences prefs = getSharedPreferences(AccelerometerDriver.APP_PREFERENCES_FILE, MODE_PRIVATE);
		
		sensorDelay = AccelerometerDriverSettings.stringDelayToConstant(this, prefs.getString(AccelerometerDriverSettings.RECORDING_DELAY, getString(R.string.recording_delay_default)));
		recordingFrequency = Long.parseLong(prefs.getString(AccelerometerDriverSettings.RECORDING_FREQUENCY, getString(R.string.recording_frequency_default)));
		broadcastFrequency = Long.parseLong(prefs.getString(AccelerometerDriverSettings.BROADCAST_FREQUENCY, getString(R.string.broadcast_frequency_default)));		
		
        return mMessenger.getBinder();
    }
	
    @Override
    public void onCreate() {
    	Log.d(TAG, "onCreate");
    	
		manager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

	@Override
    public void onDestroy() {
		Log.d(TAG, "onDestroy");
		
		manager.unregisterListener(this);
    }	

	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.d(TAG, "onSensorChanged");
		final long now = System.currentTimeMillis();

		if (now - lastObservationRecorded <= recordingFrequency) {
			return;
		}
		Log.d(TAG, "onSensorChanged recorded");
		
		lastObservationRecorded = now;
		
		observations.add(accelerometerObservationFactory(now, event.values));
		
		if (now - lastObservationBroadcasted <= broadcastFrequency) {
			return;
		}
		Log.d(TAG, "onSensorChanged broadcasted");
		
		lastObservationBroadcasted = now;
		
		broadcastObservation();
	}

	private void broadcastObservation() {
		final Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(
				DriverInterface.MSG_FIELD_OBSERVATIONS, 
				//observations.toArray(new GenericObservation [observations.size()]));
				observations);
		
		for (int i = clients.size() - 1; i >= 0; i--) {
			try {
				final Message msg = Message.obtain(
						null, 
						DriverInterface.MSG_OBSERVATION);
				
				msg.setData(bundle);
				
				clients.get(i).send(msg);
			} catch (RemoteException e) {
				clients.remove(i);
			}
		}
		
		observations.clear();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	protected GenericObservation accelerometerObservationFactory(long time, float [] values) {
		byte [][] data = new byte[3][4];
		DataTypes.floatToByteArray(values[0], data[0], 0);
		DataTypes.floatToByteArray(values[1], data[1], 0);
		DataTypes.floatToByteArray(values[2], data[2], 0);
		
		return new GenericObservation(accelerometerDataType, time, data);		
	}
	
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DriverInterface.MSG_REGISTER_CLIENT:
				Log.d(TAG, "MSG_REGISTER_CLIENT");
				
				clients.add(msg.replyTo);
				
				final Bundle payload = msg.getData();
				final String [] dataTypes = payload.getStringArray(DriverInterface.MSG_FIELD_DATA_TYPES);
				final long [] dataTypeIds = payload.getLongArray(DriverInterface.MSG_FIELD_DATA_TYPE_IDS);
				
				if (dataTypes.length > 0 && dataTypes[0].equals(DriverInterface.TYPE_ACCELEROMETER)) {
					accelerometerDataType = dataTypeIds[0];
					Log.d(TAG, "Assigned data type id" + accelerometerDataType);
				} else {
					Log.d(TAG, "Assigned data type assigned");
				}
				
				Log.d(TAG, sensorDelay + "  ");
				
				boolean res = manager.registerListener(
						AccelerometerDriver.this, 
						manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
						sensorDelay);
				
				Log.d(TAG, "registerListener result: " + res);
				
				break;
			case DriverInterface.MSG_UNREGISTER_CLIENT:
				Log.d(TAG, "MSG_UNREGISTER_CLIENT");

				clients.remove(msg.replyTo);
				
				if (clients.size() == 0) {
					AccelerometerDriver.this.stopSelf();
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
