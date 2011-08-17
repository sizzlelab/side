/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
