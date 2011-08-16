package fi.hut.soberit.sensors.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.Storage;

public abstract class StorageService extends DriverListenerService {
	
	public static class Discover extends BroadcastReceiver {

		private final String TAG = this.getClass().getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive " + intent.getAction());
			
			if(!DriverInterface.ACTION_START_STORAGE_DISCOVERY.equals(intent.getAction())) {
				return;
			}

			for (Storage storage: getStorages(context)) {
				final Intent storageDiscovered = new Intent();
				storageDiscovered.setAction(DriverInterface.ACTION_STORAGE_DISCOVERED);

				storageDiscovered.putExtra(DriverInterface.INTENT_FIELD_STORAGES, storage);
				
				Log.d(TAG, storageDiscovered.getAction());
				context.sendBroadcast(storageDiscovered);				
			}
		}
		
		public Storage[] getStorages(Context context) {
			return null;
		}
	}
}
