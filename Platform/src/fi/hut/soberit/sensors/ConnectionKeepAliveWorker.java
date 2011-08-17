/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class ConnectionKeepAliveWorker implements Runnable {

	private static final long CHECK_FREQUENCY = 5000;
	private List<DriverConnection> connections;
	private Context context;

	private Handler refreshTimeHandler;
	
	public ConnectionKeepAliveWorker(List<DriverConnection> connections, Context context) {
		this.connections = connections;
		this.context = context;
		
		refreshTimeHandler = new Handler();
		 
		refreshTimeHandler.postDelayed(this, CHECK_FREQUENCY);
	}

	@Override
	public void run() {
		try {
		
			for(DriverConnection conn: connections) {
				
				try {
					Log.d((String)context.getClass().getField("TAG").get(String.class), conn.toString());
				} catch (SecurityException e) {

					e.printStackTrace();
				} catch (NoSuchFieldException e) {

					e.printStackTrace();
				} catch (IllegalArgumentException e) {

					e.printStackTrace();
				} catch (IllegalAccessException e) {

					e.printStackTrace();
				}
				
				if (!conn.isServiceConnected()) {
					conn.bind(context);
				}
			}
		} finally {
			refreshTimeHandler.postDelayed(this, CHECK_FREQUENCY);
		}
		
	}
	
	public void stop() {
		refreshTimeHandler.removeCallbacks(this);
	}

	public List<DriverConnection> getConnections() {
		return connections;
	}

	public void setConnections(List<DriverConnection> connections) {
		this.connections = connections;
	}

}
