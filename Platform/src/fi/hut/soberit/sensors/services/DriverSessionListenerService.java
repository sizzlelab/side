/*******************************************************************************
 * Copyright (c) 2011 Aalto University
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
import android.content.IntentFilter;
import android.util.Log;
import fi.hut.soberit.sensors.DriverInterface;

public abstract class DriverSessionListenerService extends BroadcastListenerService {

	private IntentFilter sessionStopBroadcastFilter;
	private StopSessionReceiver stopSessionReceiver;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		int res = super.onStartCommand(intent, flags, startId);
		if (intent == null) {
			return res;
		}
		
		sessionStopBroadcastFilter = new IntentFilter();
		sessionStopBroadcastFilter.addAction(DriverInterface.ACTION_SESSION_STOP);
		
		stopSessionReceiver = new StopSessionReceiver();
		registerReceiver(stopSessionReceiver, sessionStopBroadcastFilter);
		
		return res;
    }
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		
		if (sessionStopBroadcastFilter != null) {
			unregisterReceiver(stopSessionReceiver);
		}
	}
	
	
	public class StopSessionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive" + intent.getAction());
			
			DriverSessionListenerService.this.stopSelf();
		}
	}
}
