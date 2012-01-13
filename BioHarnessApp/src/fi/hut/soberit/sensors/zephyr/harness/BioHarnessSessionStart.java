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
package fi.hut.soberit.sensors.zephyr.harness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.SessionBroadcastReceiver;
import fi.hut.soberit.sensors.generic.Uploader;
import fi.hut.soberit.sensors.harness.BioHarnessBroadcaster;


public class BioHarnessSessionStart extends SessionBroadcastReceiver {

	boolean startOnce = false;
	
	@Override
	protected void startDriverService(Context context, Intent broadcastIntent, Driver driver) {
		final Intent startDriver = new Intent();
		
		final SharedPreferences prefs = context.getSharedPreferences(
				BioHarnessSettings.APP_PREFERENCES_FILE, 
				Context.MODE_PRIVATE);
				
		startDriver.putExtra(BroadcastingService.INTENT_BROADCAST_FREQUENCY, 
				Long.parseLong(prefs.getString(
					BioHarnessSettings.BROADCAST_FREQUENCY,
					context.getString(R.string.broadcast_frequency_default))));
		
		startDriver.putExtra(BioHarnessBroadcaster.INTENT_DEVICE_ADDRESS, 
				prefs.getString(BioHarnessSettings.BLUETOOTH_DEVICE_ADDRESS, ""));
		
		startDriver.setAction(BioHarnessBroadcaster.ACTION);
		context.startService(startDriver);
	}
	
	
	@Override
	protected boolean isInterestingUploader(Uploader uploader) {

		return false;
	}
	
	@Override
	protected boolean isInterestingDriver(Driver driver) {

		boolean matched = BioHarnessBroadcaster.ACTION.equals(driver.getUrl());
		Log.d(TAG, String.format("matched ? [%s, %b]", driver.getUrl(), matched));

		return matched;
	}

}
