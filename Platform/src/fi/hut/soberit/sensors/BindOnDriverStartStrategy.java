/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BindOnDriverStartStrategy extends BroadcastReceiver {
	
	private static final String TAG = BindOnDriverStartStrategy.class.getSimpleName();
	
	private ArrayList<DriverConnection> connections;

	public BindOnDriverStartStrategy(ArrayList<DriverConnection> connections) {
		this.connections = connections;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "PingBackRecever.onReceive " + intent.getAction());
		final String action = intent.getAction();
		
		final String driverUrl = action.substring(0, action.length() - BroadcastingService.STARTED_PREFIX.length());
		
		for(DriverConnection connection: connections) {
			if (!driverUrl.equals(connection.getDriver().getUrl())) {
				continue;
			}
			
			Log.d(TAG, "Binding to " + connection.getDriver().getUrl());
			Log.d(TAG, "result: " + context.bindService(intent, connection, Context.BIND_DEBUG_UNBIND));	
			
			return;
		}
	}		
}
