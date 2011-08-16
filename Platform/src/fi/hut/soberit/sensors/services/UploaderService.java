package fi.hut.soberit.sensors.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.Uploader;

public abstract class UploaderService extends DriverSessionListenerService {

	
	public static class Discover extends BroadcastReceiver {

		private static final String TAG = UploaderService.class.getSimpleName() + " " + Discover.class.getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive " + intent.getAction());
			
			if(!DriverInterface.ACTION_START_UPLOADER_DISCOVERY.equals(intent.getAction())) {
				return;
			}

			for (Uploader uploader: getUploaders(context)) {
				final Intent uploaderDiscovered = new Intent();
				uploaderDiscovered.setAction(DriverInterface.ACTION_UPLOADER_DISCOVERED);

				uploaderDiscovered.putExtra(DriverInterface.INTENT_FIELD_UPLOADER, uploader);
				
				context.sendBroadcast(uploaderDiscovered);				
			}
		}
		
		public Uploader[] getUploaders(Context context) {
			return null;
		}
	}
}

