package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.List;

import eu.mobileguild.utils.DataTypes;
import fi.hut.soberit.sensors.generic.AccelerometerObservation;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationRecord;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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

	ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	ArrayList<GenericObservation> observations = new ArrayList<GenericObservation>();
	
	public int accelerometerDataType;

	public final static int ACCELEROMETER_UPDATE_RATE = SensorManager.SENSOR_DELAY_NORMAL;

	private static final long ACKNOWLEDGEMENT_FREQUENCY = 1000;
	
	private long lastObservationAcknowledgement = 0 ;

    
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DriverInterface.MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				
				final Bundle payload = msg.getData();
				final String [] dataTypes = payload.getStringArray(DriverInterface.MSG_FIELD_DATA_TYPES);
				final int [] dataTypeIds = payload.getIntArray(DriverInterface.MSG_FIELD_DATE_TYPE_IDS);
				
				if (dataTypes.length > 0 && dataTypes[0].equals(DriverInterface.TYPE_ACCELEROMETER)) {
					accelerometerDataType = dataTypeIds[0];
				}
				
				if (mClients.size() > 1) {
					break;
				}
				
				final SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
				manager.registerListener(
						AccelerometerDriver.this, 
						manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
						ACCELEROMETER_UPDATE_RATE);
				
				
				break;
			case DriverInterface.MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
//			case DriverInterface.MSG_OBSERVATION:
//				mValue = msg.arg1;
//				for (int i = mClients.size() - 1; i >= 0; i--) {
//					try {
//						mClients.get(i).send(
//								Message.obtain(null, DriverInterface.MSG_OBSERVATION, mValue, 0));
//					} catch (RemoteException e) {
//						// The client is dead. Remove it from the list;
//						// we are going through the list from back to front
//						// so this is safe to do inside the loop.
//						mClients.remove(i);
//					}
//				}
//				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
    final Messenger mMessenger = new Messenger(new IncomingHandler());

	private NotificationManager mNM;


    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        showNotification();
    }

	@Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.remote_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
    }
	
	public AccelerometerDriver() {

	}
	

	@Override
    public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		
        return mMessenger.getBinder();
    }

	private void showNotification() {
		Log.d(TAG, "showNotification");

		CharSequence text = getText(R.string.remote_service_started);

		Notification notification = new Notification(R.drawable.icon,
				text, System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, SIDE.class), 0);

		notification.setLatestEventInfo(this,
				getText(R.string.remote_service_label), text, contentIntent);

		mNM.notify(R.string.remote_service_started, notification);
	}

	
	protected GenericObservation accelerometerObservationFactory(long time, float [] values) {
		byte [][] data = new byte[3][4];
		DataTypes.floatToByteArray(values[0], data[0], 0);
		DataTypes.floatToByteArray(values[1], data[1], 0);
		DataTypes.floatToByteArray(values[2], data[2], 0);
		
		return new GenericObservation(time, data);		
	}
	
	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		final long now = System.currentTimeMillis();
		
		observations.add(accelerometerObservationFactory(now, event.values));
		
		if (now - lastObservationAcknowledgement <= ACKNOWLEDGEMENT_FREQUENCY) {
			return;
		}
		
		lastObservationAcknowledgement = now;
		
		final Bundle bundle = new Bundle();
		bundle.putParcelableArray(
				DriverInterface.MSG_FIELD_OBSERVATIONS, 
				observations.toArray(new GenericObservation [observations.size()]));
		observations.clear();
		
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				final Message msg = Message.obtain(
						null, 
						DriverInterface.MSG_OBSERVATION, 
						accelerometerDataType);
				
				msg.obj = null;
				msg.setData(bundle);
				
				mClients.get(i).send(msg);
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list;
				// we are going through the list from back to front
				// so this is safe to do inside the loop.
				mClients.remove(i);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
}
